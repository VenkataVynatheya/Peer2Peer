import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

public class Handshake extends Constants {

    private byte[] hsMsg;
    private int ID_peer;
    private String hsHeader;
    private String zeroBits;
    private int k;

    public byte[] getMsg() {
        return hsMsg;
    }

    public void setMsg(byte[] hsMsg) {
        this.hsMsg = hsMsg;
    }

    public int getPeerId() {
        return ID_peer;
    }

    public void setPeerId(int ID_peer) {
        this.ID_peer = ID_peer;
    }

    public String getHSHeader() {
        return hsHeader;
    }

    public void setHandShakeHeader(String hsHeader) {
        this.hsHeader = hsHeader;
    }

    public String getZeroBits() {
        return zeroBits;
    }

    public void setZeroBits(String zeroBits) {
        this.zeroBits = zeroBits;
    }

    @Override
    public String toString() {
        return "Handshake {" +
                "hsMsg=" + Arrays.toString(hsMsg) +
                '}';
    }

    public byte[] getHSHeaderBytes() {
        return hsHeaderBytes;
    }

    public void setHandShakeHeaderBytes(byte[] hsHeaderBytes) {
        this.hsHeaderBytes = hsHeaderBytes;
    }

    private byte[] hsHeaderBytes = new byte[32];

    Handshake() {

    }

    Handshake(int ID_peer) {
        this.hsMsg = new byte[32];
        this.ID_peer = ID_peer;
        this.hsHeader = Constants.handshakeHeader;
        this.zeroBits = Constants.zeroBits;
        this.k = 0;
        this.hsHeaderBytes = hsHeader.getBytes(StandardCharsets.UTF_8);
    }

    public void setMsgID_peer(byte[] ID_peerByteArray) {
        try {
            if (ID_peerByteArray == null) {
                throw new Exception("Invalid PeerID");
            }
            if (ID_peerByteArray.length > 4) {
                throw new Exception("Zero bit padding length is greater than 10");
            }

            for (int i = 0; i < ID_peerByteArray.length; i++) {
                this.hsMsg[k] = ID_peerByteArray[i];
                k++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

    }

    public void generateHandShake() {
        byte[] hsHeaderByteArray = this.hsHeader.getBytes();
        byte[] zeroBitsByteArray = this.zeroBits.getBytes(StandardCharsets.UTF_8);
        String ID_peerString = this.ID_peer + "";
        byte[] ID_peerByteArray = ID_peerString.getBytes(StandardCharsets.UTF_8);

        try {
            setMsgID_peer(ID_peerByteArray);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
        System.out.println(
                "Hand Shake message generated is : " + new String(this.hsMsg, StandardCharsets.UTF_8));
    }

}
