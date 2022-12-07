import java.util.Comparator;
import java.util.Date;

public class RemotePeerData implements Comparator<RemotePeerData> {
    public final boolean comparator = false;
    public String ID_peer;
    public String address_Peer;
    public String portNumber_Peer;
    public int isPrimary_Peer;
    public double rateOfStream = 0;
    public int isInterested_Peer = 1;
    public int isPrefNeighbour_Peer = 0;
    public int isOptionalUnchokedNeighbour_Peer = 0;
    public int peer_isChoked = 1;
    public PayLoadData payloadData;
    public int position = -1;
    public int indexOfPeer;
    public int isFinished = 0;
    public int isHandShake = 0;
    public Date time1;
    public Date time2;

    public RemotePeerData() {

    }

    public String getID_peer() {
        return ID_peer;
    }

    public void setID_peer(String ID_peer) {
        this.ID_peer = ID_peer;
    }

    public String getAddressOfPeer() {
        return address_Peer;
    }

    public void setaddress_Peer(String address_Peer) {
        this.address_Peer = address_Peer;
    }

    public String obtainPeerPortNum() {
        return portNumber_Peer;
    }

    public void setportNumber_Peer(String portNumber_Peer) {
        this.portNumber_Peer = portNumber_Peer;
    }

    public boolean isHasFile() {
        return hasFile;
    }

    public void setHasFile(boolean hasFile) {
        this.hasFile = hasFile;
    }

    public int getPositionOfPeer() {
        return PositionOfPeer;
    }

    public void setPositionOfPeer(int PositionOfPeer) {
        this.PositionOfPeer = PositionOfPeer;
    }

    public boolean hasFile;
    public int PositionOfPeer;
    public boolean isPrimary;

    public RemotePeerData(String peer_ID, String pAddress, String port_Peer, boolean file1) {
        ID_peer = peer_ID;
        address_Peer = pAddress;
        portNumber_Peer = port_Peer;
        hasFile = file1;
        payloadData = new PayLoadData();
    }

    public int compareTo(RemotePeerData remotePeer_Info) {
        return Double.compare(this.rateOfStream, remotePeer_Info.rateOfStream);
    }

    public int compare(RemotePeerData rpd1, RemotePeerData rpd2) {
        if (rpd1 == null && rpd2 == null)
            return 0;
        if (rpd1 == null) {
            return 1;
        }
        if (rpd2 == null) {
            return -1;
        }
        if (comparator) {
            return rpd1.compareTo(rpd2);
        } else {
            return rpd2.compareTo(rpd1);
        }
    }

}