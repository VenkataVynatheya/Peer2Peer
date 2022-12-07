
import java.io.*;
import java.util.*;

/*
 * The RemotePeerStart class begins remote peer processes.
 * It reads configuration file PeerInfo.cfg and starts remote peer processes.
 * You must modify this program a little bit if your peer processes are written in C or C++.
 * Please look at the lines below the comment saying IMPORTANT.
 */
public class RemotePeerStart {

    public Vector<RemotePeerData> vector_PeerData;

    public void get_Config() {
        String str1;
        int i1;
        vector_PeerData = new Vector<RemotePeerData>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("PeerInfo.cfg"));
            while ((str1 = br.readLine()) != null) {

                String[] tokens = str1.split("\\s+");

            }

            br.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            RemotePeerStart myStart = new RemotePeerStart();
            myStart.get_Config();

            // get current stream_Path
            String stream_Path = System.getProperty("user.dir");

            // start clients at remote hosts
            for (int i = 0; i < myStart.vector_PeerData.size(); i++) {
                RemotePeerData info_Peer = (RemotePeerData) myStart.vector_PeerData.elementAt(i);

                System.out.println("Start remote peer " + info_Peer.ID_peer + " at " + info_Peer.address_Peer);

                // *********************** IMPORTANT *************************** //
                // If your program is JAVA, use this line.
                Runtime.getRuntime()
                        .exec("ssh " + info_Peer.address_Peer + " cd " + stream_Path + "; java peerProcess "
                                + info_Peer.ID_peer);

                // If your program is C/C++, use this line instead of the above line.
                // Runtime.getRuntime().exec("ssh " + info_Peer.peerAddress + " cd " +
                // stream_Path +
                // ";
                // ./peerProcess " + info_Peer.ID_peer);
            }
            System.out.println("Starting all remote peers has done.");

        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

}
