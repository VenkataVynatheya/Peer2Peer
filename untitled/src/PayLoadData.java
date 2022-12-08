import java.io.*;
import java.util.Arrays;

public class PayLoadData {
    public int bit_Size;
    public static Logger log;
    public Payloadbit[] bit_Data;

    PayLoadData() {
        double d = (double) Constants.sizeOfFile / Constants.sizeOfbit;
        this.bit_Size = (int) Math.ceil(d);
        this.bit_Data = new Payloadbit[this.bit_Size];
        int ind = 0;
        while (ind < this.bit_Size) {
            this.bit_Data[ind++] = new Payloadbit();
        }
    }

    public static PayLoadData data_Decode(byte[] b) {
        PayLoadData PLD = new PayLoadData();
        int ind = 0;
        while (ind < b.length) {
            int data = 7;
            while (data >= 0) {
                int pNo = 1 << data;
                int count = ind * 8 + (8 - data - 1);
                if (count < PLD.bit_Size) {
                    if ((b[ind] & (pNo)) != 0) {
                        PLD.bit_Data[count].hasbit = 1;
                    } else {
                        PLD.bit_Data[count].hasbit = 0;
                    }
                }
                data--;
            }
            ind++;
        }
        return PLD;
    }

    public Payloadbit[] getbit_Data() {
        return bit_Data;
    }

    public void setbit_Data(Payloadbit[] bit_Data) {
        this.bit_Data = bit_Data;
    }

    public int getbit_Size() {
        return bit_Size;
    }

    public void setbit_Size(int bit_Size) {
        this.bit_Size = bit_Size;
    }

    public synchronized boolean dividePayLoadData(PayLoadData p) {
        int csize = p.getbit_Size();
        int ind = 0;
        while (ind < csize) {
            if (p.getbit_Data()[ind].getHasbit() == 1 && this.getbit_Data()[ind].getHasbit() == 0) {
                return true;
            }
            ind++;
        }
        return false;
    }

    public byte[] data_Encode() {
        int count = 0;
        int temporary = 0;
        int bit_Index = 0;
        int ind = 1;

        if (this.bit_Size % 8 != 0) {
            count += 1;
        }
        count += this.bit_Size / 8;
        byte[] bit_Array = new byte[count];

        while (ind <= this.bit_Size) {
            int t1 = this.bit_Data[ind - 1].hasbit;
            temporary = temporary << 1;
            if (t1 == 1) {
                temporary++;
            }
            if (ind % 8 == 0) {
                bit_Array[bit_Index] = (byte) temporary;
                bit_Index++;
                temporary = 0;
            }
            ind++;
        }
        ind--;
        if (ind % 8 != 0) {
            int bit_Shift = this.bit_Size - (this.bit_Size / 8) * 8;
            temporary <<= (8 - bit_Shift);
            bit_Array[bit_Index] = (byte) temporary;
        }
        return bit_Array;
    }

    public synchronized int get_firstBitField(PayLoadData p) {
        if (this.getbit_Size() >= p.getbit_Size()) {
            int ind = 0;
            while (ind < p.getbit_Size()) {
                if (p.getbit_Data()[ind].getHasbit() == 1 && this.getbit_Data()[ind].getHasbit() == 0) {
                    return ind;
                }
                ind++;
            }
        } else {
            int ind = 0;
            while (ind < this.getbit_Size()) {
                if (p.getbit_Data()[ind].getHasbit() == 1 && this.getbit_Data()[ind].getHasbit() == 0) {
                    return ind;
                }
                ind++;
            }
        }
        return -1;
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

    public boolean contains_allBits() {

        for (int i = 0; i < this.bit_Size; i++) {
            if (this.bit_Data[i].hasbit == 0) {
                return false;
            }
        }
        return true;

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
        if (Peer2Peer.payLoadCurrent.bit_Data[p.peer_Index].hasbit == 1) {
            Peer2Peer.logger.logDisplay(" This bit already exists - " + pId);
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
                Peer2Peer.logger.logDisplay("Peer " +
                        Peer2Peer.ID_peer + " has downloaded the bits " + p.peer_Index + " from Peer ID: " + pId
                        + ". It now contains " + Peer2Peer.payLoadCurrent.avaliablebits() + " bits");
                if (Peer2Peer.payLoadCurrent.contains_allBits()) {
                    Peer2Peer.hm_peerData.get(Peer2Peer.ID_peer).isInterested_Peer = 0;
                    Peer2Peer.hm_peerData.get(Peer2Peer.ID_peer).isFinished = 1;
                    Peer2Peer.hm_peerData.get(Peer2Peer.ID_peer).peer_isChoked = 0;
                    refresh_peerConfig(Peer2Peer.ID_peer);
                    Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " has completed downloading the file!!!");
                    Peer2Peer.logger.logDisplay("Peer ID: " + Peer2Peer.ID_peer + " sent NOT INTERESTED MESSAGE");
                }
            } catch (Exception ex) {
                Peer2Peer.logger.logDisplay(ex.getMessage());
            }
        }
    }

    public void refresh_peerConfig(String pId) {
        BufferedReader br;
        BufferedWriter buffWriter;
        String st0 = "";
        String st1;

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
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

}

class Payloadbit {
    public byte[] bit;
    public int peer_Index;
    public int hasbit;
    public String senderpId;

    public static Payloadbit convertTobit(byte[] data) {
        int max = Constants.maxbitLength;
        Payloadbit payloadbit = new Payloadbit();
        byte[] byteArr = new byte[max];
        System.arraycopy(data, 0, byteArr, 0, max);
        payloadbit.peer_Index = Constants.byteToIntConverter(byteArr, 0);
        payloadbit.bit = new byte[data.length - max];
        System.arraycopy(data, max, payloadbit.bit, 0, data.length - max);
        return payloadbit;
    }

    public Payloadbit() {
        hasbit = 0;
        senderpId = null;
        bit = new byte[Constants.sizeOfbit];
        peer_Index = -1;
    }

    public String getSenderpId() {
        return senderpId;
    }

    public void setSenderpId(String senderpId) {
        this.senderpId = senderpId;
    }

    public int getHasbit() {
        return hasbit;
    }

    public void setHasbit(int bit) {
        this.hasbit = bit;
    }
}
