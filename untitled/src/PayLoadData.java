import java.io.*;
import java.util.Arrays;

public class PayLoadData {
    public Payloadbit[] bit_Data;
    public int bit_Size;
    public static Logger log;

    PayLoadData() {
        double d = (double) Constants.sizeOfFile / Constants.sizeOfbit;
        this.bit_Size = (int) Math.ceil(d);
        this.bit_Data = new Payloadbit[this.bit_Size];
        int i = 0;
        while (i < this.bit_Size) {
            this.bit_Data[i++] = new Payloadbit();
        }
    }

    public int getbit_Size() {
        return bit_Size;
    }

    public void setbit_Size(int bit_Size) {
        this.bit_Size = bit_Size;
    }

    public Payloadbit[] getbit_Data() {
        return bit_Data;
    }

    public void setbit_Data(Payloadbit[] bit_Data) {
        this.bit_Data = bit_Data;
    }

    public synchronized boolean dividePayLoadData(PayLoadData p) {
        int csize = p.getbit_Size();
        int i = 0;
        while (i < csize) {
            if (p.getbit_Data()[i].getHasbit() == 1 && this.getbit_Data()[i].getHasbit() == 0) {
                return true;
            }
            i++;
        }
        return false;
    }

    public synchronized int get_firstBitField(PayLoadData p) {
        if (this.getbit_Size() >= p.getbit_Size()) {
            int i = 0;
            while (i < p.getbit_Size()) {
                if (p.getbit_Data()[i].getHasbit() == 1 && this.getbit_Data()[i].getHasbit() == 0) {
                    return i;
                }
                i++;
            }
        } else {
            int i = 0;
            while (i < this.getbit_Size()) {
                if (p.getbit_Data()[i].getHasbit() == 1 && this.getbit_Data()[i].getHasbit() == 0) {
                    return i;
                }
                i++;
            }
        }
        return -1;
    }

    public byte[] data_Encode() {
        int s = 0;
        if (this.bit_Size % 8 != 0) {
            s += 1;
        }
        s += this.bit_Size / 8;
        byte[] bit_Array = new byte[s];
        int temporary = 0;
        int bit_Index = 0;
        int i = 1;
        while (i <= this.bit_Size) {
            int t1 = this.bit_Data[i - 1].hasbit;
            temporary = temporary << 1;
            if (t1 == 1) {
                temporary++;
            }
            if (i % 8 == 0) {
                bit_Array[bit_Index] = (byte) temporary;
                bit_Index++;
                temporary = 0;
            }
            i++;
        }
        i--;
        if (i % 8 != 0) {
            int bit_Shift = this.bit_Size - (this.bit_Size / 8) * 8;
            temporary <<= (8 - bit_Shift);
            bit_Array[bit_Index] = (byte) temporary;
        }
        return bit_Array;
    }

    public static PayLoadData data_Decode(byte[] b) {
        PayLoadData PLD = new PayLoadData();
        int i = 0;
        while (i < b.length) {
            int c = 7;
            while (c >= 0) {
                int pNo = 1 << c;
                int k = i * 8 + (8 - c - 1);
                if (k < PLD.bit_Size) {

                    if ((b[i] & (pNo)) != 0) {
                        PLD.bit_Data[k].hasbit = 1;
                    } else {
                        PLD.bit_Data[k].hasbit = 0;
                    }
                }
                c--;
            }
            i++;
        }
        return PLD;
    }

    public boolean contains_allBits() {

        for (int i = 0; i < this.bit_Size; i++) {
            if (this.bit_Data[i].hasbit == 0) {
                return false;
            }
        }
        return true;

    }

    public void initiatePayload(String pId, boolean hasFile) {
        int i = 0;
        if (hasFile) {
            while (i < bit_Size) {
                this.bit_Data[i].setHasbit(1);
                this.bit_Data[i].setSenderpId(pId);
                i++;
            }

        } else {
            while (i < bit_Size) {
                this.bit_Data[i].setHasbit(0);
                this.bit_Data[i].setSenderpId(pId);
                i++;
            }
        }

    }

    public int avaliablebits() {
        int avlbits = 0;
        for (int i = 0; i < this.bit_Size; i++) {
            if (this.bit_Data[i].hasbit == 1) {
                avlbits += 1;
            }
        }
        return avlbits;
    }

    public synchronized void refresh_payLoad(Payloadbit p, String pId) {
        Peer2Peer.logger.logDisplay("Refresh payload entered - " + p.bit.toString() + " " + pId);
        if (Peer2Peer.payLoadCurrent.bit_Data[p.peer_Index].hasbit == 1) {
            Peer2Peer.logger.logDisplay(pId + " This bit already exists");
        } else {
            try {
                byte[] output_Data;
                int balance = p.peer_Index * Constants.sizeOfbit;
                File f = new File(Peer2Peer.ID_peer, Constants.fileDesc);
                RandomAccessFile r = new RandomAccessFile(f, "rw");
                output_Data = p.bit;
                r.seek(balance);
                r.write(output_Data);
                r.close();
                this.bit_Data[p.peer_Index].setHasbit(1);
                this.bit_Data[p.peer_Index].setSenderpId(pId);
                Peer2Peer.logger.logDisplay(
                        Peer2Peer.ID_peer + " Peer has downloaded the bit " + p.peer_Index + " from peer " + pId
                                + ". It now contains " + Peer2Peer.payLoadCurrent.avaliablebits() + " bits");
                if (Peer2Peer.payLoadCurrent.contains_allBits()) {
                    Peer2Peer.hm_peerData.get(Peer2Peer.ID_peer).isInterested_Peer = 0;
                    Peer2Peer.hm_peerData.get(Peer2Peer.ID_peer).isFinished = 1;
                    Peer2Peer.hm_peerData.get(Peer2Peer.ID_peer).peer_isChoked = 0;
                    refresh_peerConfig(Peer2Peer.ID_peer);
                    Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " has completed downloading the file!!!");
                    Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " is sending NOT INTERESTED MESSAGE");
                }
            } catch (Exception ex) {
                Peer2Peer.logger.logDisplay(ex.getMessage());
            }
        }
    }

    public void refresh_peerConfig(String pId) {
        Peer2Peer.logger.logDisplay("Refresh peer configg - " + pId);
        String st0 = "";
        String st1;
        BufferedReader br;
        BufferedWriter buffWriter;
        try {
            br = new BufferedReader(new FileReader(Constants.PEERS_PATH));
            while ((st1 = br.readLine()) != null) {
                String[] strArr = st1.trim().split(" ");
                Peer2Peer.logger.logDisplay(Arrays.toString(strArr));
                if (strArr[0].equals(pId)) {
                    strArr[3] = "1";
                    st1 = strArr[0] + " " + strArr[1] + " " + strArr[2] + " " + strArr[3];
                }
                st0 += st1 + "\n";
            }
            br.close();
            buffWriter = new BufferedWriter(new FileWriter(Constants.PEERS_PATH));
            buffWriter.write(st0);
            buffWriter.close();
            Peer2Peer.logger.logDisplay("sto -- " + st0);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

}

class Payloadbit {
    public int hasbit;
    public String senderpId;
    public byte[] bit;
    public int peer_Index;

    public int getHasbit() {
        return hasbit;
    }

    public void setHasbit(int bit) {
        this.hasbit = bit;
    }

    public String getSenderpId() {
        return senderpId;
    }

    public void setSenderpId(String senderpId) {
        this.senderpId = senderpId;
    }

    public static Payloadbit convertTobit(byte[] data) {
        int a = Constants.maxbitLength;
        Payloadbit p = new Payloadbit();
        byte[] b = new byte[a];
        System.arraycopy(data, 0, b, 0, a);
        p.peer_Index = Constants.byteToIntConverter(b, 0);
        p.bit = new byte[data.length - a];
        System.arraycopy(data, a, p.bit, 0, data.length - a);
        return p;
    }

    public Payloadbit() {
        bit = new byte[Constants.sizeOfbit];
        peer_Index = -1;
        hasbit = 0;
        senderpId = null;
    }

}
