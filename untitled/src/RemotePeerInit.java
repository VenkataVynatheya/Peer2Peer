
import java.io.*;
import java.util.*;

/*
 * Remote peer processes are started via the RemotePeerInit class. 
 * It launches remote peer processes after reading the configuration file PeerInfo.cfg..
 */
public class RemotePeerInit {

    public Vector<RemotePeerData> vector_peerInfo;

    public void getConfiguration() {
        String srp;
        int i1;
        vector_peerInfo = new Vector<RemotePeerData>();
        try {
            BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
            while ((srp = in.readLine()) != null) {

                String[] tokens = srp.split("\\s+");

            }

            in.close();
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
            RemotePeerInit startFlag = new RemotePeerInit();
            startFlag.getConfiguration();

            // get current path
            String path = System.getProperty("user.dir");

            // start clients at remote hosts
            for (int i = 0; i < startFlag.vector_peerInfo.size(); i++) {
                RemotePeerData pInfo = (RemotePeerData) startFlag.vector_peerInfo.elementAt(i);

                System.out.println("Start remote peer " + pInfo.peerID + " at " + pInfo.peerAddress);

                Runtime.getRuntime()
                        .exec("ssh " + pInfo.peerAddress + " cd " + path + "; java peerProcess " + pInfo.peerID);

            }
            System.out.println("Starting all remote peers has done.");

        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

}
