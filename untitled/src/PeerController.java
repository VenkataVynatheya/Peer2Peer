import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class PeerController implements Runnable {
    private InputStream ip_Stream;
    private OutputStream op_Stream;
    public static final int activeSession_Count = 1;
    private Socket socket = null;
    private int typeOfSession;
    String remote_PeerID;
    String current_PeerID;

    public void run() {
        DataParameters dataparams = new DataParameters();
        byte[] array_msgHandShake = new byte[32];
        byte[] array_bufmsg = new byte[Constants.message_Size + Constants.message_Type];
        byte[] size_data;
        byte[] type_data;
        try {
            if (this.typeOfSession != activeSession_Count) {
                refresh_peerData(array_msgHandShake);
                if (peerConnect()) {
                    throw new Exception("Failed to connect with : " + this.current_PeerID);
                }
                Peer2Peer.logger.logDisplay(this.current_PeerID + " is sending HandShake to " + this.remote_PeerID);
                Peer2Peer.logger
                        .logDisplay(this.current_PeerID + " makes a TCP connection with peer " + this.remote_PeerID);
                Peer2Peer.hm_peerData.get(remote_PeerID).position = 2;

            } else {
                if (peerConnect()) {
                    throw new Exception("Failed to connect with : " + this.current_PeerID);
                }
                refresh_peerData(array_msgHandShake);
                Peer2Peer.logger.logDisplay(this.current_PeerID + " is sending HandShake to " + this.remote_PeerID);
                Peer2Peer.hm_peerData.get(remote_PeerID).position = 8;
                MessageInfo md = new MessageInfo(Constants.bitField, Peer2Peer.payLoadCurrent.data_Encode());
                op_Stream.write(MessageInfo.array_DataToByte(md));
            }
            x: while (true) {
                int HB;
                if ((HB = ip_Stream.read(array_bufmsg)) == -1) {
                    break x;
                }
                size_data = new byte[Constants.message_Size];
                type_data = new byte[Constants.message_Type];
                System.arraycopy(array_bufmsg, 0, size_data, 0, Constants.message_Size);
                System.arraycopy(array_bufmsg, Constants.message_Size, type_data, 0, Constants.message_Type);
                MessageInfo mi = new MessageInfo();
                mi.setdata_Size(size_data);
                mi.setdata_Type(type_data);
                String str0 = "0 1 2 3";
                if (str0.contains(mi.fetchdata_Type())) {
                    dataparams.msginfo = mi;
                } else {
                    int byte_Read = 0;
                    int bytes_tobeRead;
                    byte[] payloadMessage = new byte[mi.getsize_Message() - 1];
                    while (byte_Read < mi.getsize_Message() - 1) {
                        bytes_tobeRead = ip_Stream.read(payloadMessage, byte_Read,
                                mi.getsize_Message() - 1 - byte_Read);
                        if (bytes_tobeRead == -1) {
                            return;
                        }
                        byte_Read += bytes_tobeRead;
                    }
                    byte[] MessageInfoPayLoad = new byte[mi.getsize_Message() + Constants.message_Size];
                    System.arraycopy(array_bufmsg, 0, MessageInfoPayLoad, 0,
                            Constants.message_Size + Constants.message_Type);
                    System.arraycopy(payloadMessage, 0, MessageInfoPayLoad,
                            Constants.message_Size + Constants.message_Type, payloadMessage.length);
                    dataparams.msginfo = MessageInfo.array_ByteToData(MessageInfoPayLoad);
                }
                dataparams.ID_Peer = this.remote_PeerID;
                Peer2Peer.appendToQueue(dataparams);
            }
        } catch (Exception ex) {
            Peer2Peer.logger.logDisplay(ex.getMessage());
        }

    }

    public void refresh_peerData(byte[] peerData_Array) throws IOException {
        x: while (1 == 1) {
            ip_Stream.read(peerData_Array);
            String str1 = new String(peerData_Array, StandardCharsets.UTF_8);
            // Handshake h = new Handshake.byteToHandShake(peerData_Array);
            if (str1.substring(0, 18).equals(Constants.handshakeHeader)) {
                remote_PeerID = str1.substring(str1.length() - 4, str1.length());
                Peer2Peer.logger
                        .logDisplay(
                                this.current_PeerID + " has received a handshake message from " + this.remote_PeerID);
                Peer2Peer.pD.put(this.remote_PeerID, this.socket);
                break x;
            }
        }

    }

    PeerController(String ID_Peer, Socket s, int typeOfSession) {
        this.socket = s;
        this.typeOfSession = typeOfSession;
        this.current_PeerID = ID_Peer;
        try {
            ip_Stream = s.getInputStream();
            op_Stream = s.getOutputStream();
        } catch (IOException e) {
            Peer2Peer.logger.logDisplay(this.current_PeerID + " error occurred when trying to get Data.");
        }
    }

    PeerController(String host, int port, int typeOfSession, String ID_Peer) throws IOException {
        this.typeOfSession = typeOfSession;
        try {
            this.current_PeerID = ID_Peer;
            this.socket = new Socket(host, port);
        } catch (Exception ex) {
            Peer2Peer.logger.logDisplay("Error occurred when trying to open a connection with peer " + ID_Peer);
        }
        try {
            ip_Stream = socket.getInputStream();
            op_Stream = socket.getOutputStream();
        } catch (IOException e) {
            Peer2Peer.logger.logDisplay(this.current_PeerID + " error occurred when trying to get Data.");
        }
    }

    public boolean peerConnect() {
        try {
            op_Stream.write(Handshake.hsk_toArray(new Handshake(Integer.parseInt(this.current_PeerID))));
        } catch (Exception ex) {
            Peer2Peer.logger.logDisplay("Sending HandShake Failed.....");
            return true;
        }
        return false;
    }

}
