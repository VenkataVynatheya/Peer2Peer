import java.io.*;
import java.util.*;

/*
 * This class starts the remote peer processes by reading the PeerInfo.cfg config file.
 */
public class RemotePeerStart {

    public Vector<RemotePeerData> vector_PeerData;

    /**
     * Input parameters = args
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            RemotePeerStart startPeers = new RemotePeerStart();
            startPeers.get_Config();

            // Fetch stream_path
            String stream_path = System.getProperty("user.dir");

            // Clients starting at remote hosts
            for (int ind = 0; ind < startPeers.vector_PeerData.size(); ind++) {
                RemotePeerData info_Peer = (RemotePeerData) startPeers.vector_PeerData.elementAt(ind);
                System.out.println(
                        "Remote Peer starting -> Peer ID:" + info_Peer.ID_peer + " at " + info_Peer.address_Peer);

                Runtime.getRuntime()
                        .exec("ssh " + info_Peer.address_Peer + " cd " + stream_path + "; java peerProcess "
                                + info_Peer.ID_peer);
            }
            System.out.println("Started all remote peers.");

        } catch (Exception exception) {
            System.out.println(exception);
        }
    }

    public void get_Config() {
        String str1;
        vector_PeerData = new Vector<RemotePeerData>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("PeerInfo.cfg"));
            while ((str1 = br.readLine()) != null) {
                String[] tokens = str1.split("\\s+");
            }
            br.close();
        } catch (Exception exception) {
            System.out.println(exception.toString());
        }
    }

}
