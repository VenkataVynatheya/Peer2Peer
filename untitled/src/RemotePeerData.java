import java.util.Comparator;

public class RemotePeerData implements Comparator<RemotePeerData> {
    public final boolean comparator = false;
    public String peerID;
    public String peerAddress;
    public String peerPortNum;

    public double rateOfDataStream = 0;
    public int isNeighPreferred = 0;

    public PayLoadInfo payloadPiece;

    public int isDone = 0;
    public int isHandShake = 0;
    public boolean hasFile;

    public RemotePeerData() {

    }

    public String getPeerAddress() {
        return peerAddress;
    }

    public String getPeerPortNumber() {
        return peerPortNum;
    }

    public RemotePeerData(String peerId, String pAddress, String peerPort, boolean isFile) {
        peerID = peerId;
        peerAddress = pAddress;
        peerPortNum = peerPort;
        hasFile = isFile;
        payloadPiece = new PayLoadInfo();
    }

    public int compareTo(RemotePeerData remotePeerData) {
        return Double.compare(this.rateOfDataStream, remotePeerData.rateOfDataStream);
    }

    public int compare(RemotePeerData peer1, RemotePeerData peer2) {
        if (peer1 == null && peer2 == null)
            return 0;
        if (peer1 == null)
            return 1;
        if (peer2 == null)
            return -1;
        if (comparator) {
            return peer1.compareTo(peer2);
        } else {
            return peer2.compareTo(peer1);
        }
    }

}