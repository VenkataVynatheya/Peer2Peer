import java.util.Comparator;

public class RemotePeerData implements Comparator<RemotePeerData>
{
    public final boolean comparator=false;
    public String ID_Peer;
    public String peerAddress;
    public String peerPort;

    public double streamRate = 0;
    public int isInterested = 1;
    public int isPreferredNeighbor = 0;


    public PayLoadData payloadData;

    public int isCompleted = 0;
    public int isHandShake = 0;


    public RemotePeerData() {

    }


    public String getPeerAddress() {
        return peerAddress;
    }


    public String getPeerPort() {
        return peerPort;
    }





    public boolean fileExists;
    public int peerPos;
    public boolean isFirst;
    public RemotePeerData(String pId, String pAddress, String pPort,boolean hFile) {
        ID_Peer = pId;
        peerAddress = pAddress;
        peerPort = pPort;
        fileExists=hFile;
        payloadData =new PayLoadData();
    }
    public int compareTo(RemotePeerData RemotePeerData)
    {
        return Double.compare(this.streamRate,RemotePeerData.streamRate);
    }
    public int compare(RemotePeerData p1,RemotePeerData p2)
    {
        if(p1==null && p2==null)
            return 0;
        if(p1==null)
        {
            return 1;
        }
        if(p2==null)
        {
            return -1;
        }
        if(comparator)
        {
            return p1.compareTo(p2);
        }
        else
        {
            return p2.compareTo(p1);
        }
    }

}