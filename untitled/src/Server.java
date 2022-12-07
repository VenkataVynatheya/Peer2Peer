import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    private final ServerSocket socket;
    private final String ID_Peer;
    Socket socket_Remote;
    Thread t1;

    public Server(ServerSocket s, String ID_Peer) {
        this.socket = s;
        this.ID_Peer = ID_Peer;
    }

    public void run() {
        while (true) {
            try {
                socket_Remote = socket.accept();
                t1 = new Thread(new PeerController(this.ID_Peer, socket_Remote, 0));
                Peer2Peer.vector.add(t1);
                t1.start();
            } catch (Exception ex) {
                Peer2Peer.logger.logDisplay(this.ID_Peer + " an Exception when trying to establish a connection");
            }
        }
    }
}
