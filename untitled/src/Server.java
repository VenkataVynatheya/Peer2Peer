import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    private final ServerSocket serverSocket;
    private final String peerID;
    Socket remoteSocketConnection;
    Thread t;

    public Server(ServerSocket socket, String peerId) {
        this.serverSocket = socket;
        this.peerID = peerId;
    }

    public void run() {
        while (true) {
            try {
                remoteSocketConnection = serverSocket.accept();
                t = new Thread((Runnable) new PeerController(this.peerID, remoteSocketConnection, 0));
                Peer2Peer.vector.add(t);
                t.start();
            } catch (Exception exception) {
                Peer2Peer.logger
                        .logDisplay("Exception occurred while establishing a connection with Peer ID: " + this.peerID);
            }
        }
    }
}
