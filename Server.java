import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable
{
    private final ServerSocket socket;
    private final String peerId;
    Socket remoteSocket;
    Thread thread;
    public Server(ServerSocket s,String peerId)
    {
        this.socket=s;
        this.peerId=peerId;
    }
    public void run()
    {
        while(true)
        {
            try
            {
                remoteSocket=socket.accept();
                thread=new Thread((Runnable) new PeerController(this.peerId,remoteSocket,0));
                Peer2Peer.nv.add(thread);
                thread.start();
            }
            catch (Exception ex)
            {
                Peer2Peer.logger.logDisplay(this.peerId+" an Exception when trying to establish a connection");
            }
        }
    }
}
