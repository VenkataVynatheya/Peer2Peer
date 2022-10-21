import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class PeerController {
    private InputStream input;
    private OutputStream output;

    private Socket socket = null;
    private int typeOfPeerSession;

    String currPeerID;

    PeerController(String peerID, Socket socket, int sessionType) {
        this.socket = socket;
        this.typeOfPeerSession = sessionType;
        this.currPeerID = peerID;
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
        } catch (IOException exception) {
            Peer2Peer.logger.logDisplay("Error while fetching data of Peer: " + this.currPeerID);
        }
    }

    PeerController(String host, int portNumber, int sessionType, String peerID) throws IOException {
        this.typeOfPeerSession = sessionType;
        try {
            this.currPeerID = peerID;
            this.socket = new Socket(host, portNumber);
        } catch (Exception exception) {
            Peer2Peer.logger.logDisplay("Error occurred while fetching Peer: " + peerID + " connections.");
        }
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
        } catch (IOException exception) {
            Peer2Peer.logger.logDisplay("Error while fetching data of Peer: " + this.currPeerID);
        }
    }
}
