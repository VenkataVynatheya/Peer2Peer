import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class PeerController {
    private InputStream ip;
    private OutputStream op;

    private Socket socket=null;
    private int sessionType;

    String currentpId;
    PeerController(String pId,Socket s,int sessionType)
    {
        this.socket=s;
        this.sessionType=sessionType;
        this.currentpId=pId;
        try
        {
            ip=s.getInputStream();
            op=s.getOutputStream();
        }
        catch (IOException e) {
            Peer2Peer.logger.logDisplay(this.currentpId+" error occurred when trying to get Data.");
        }
    }
    PeerController(String host,int port,int sessionType,String pId) throws IOException
    {
        this.sessionType=sessionType;
        try
        {
            this.currentpId=pId;
            this.socket=new Socket(host,port);
        }
        catch (Exception ex)
        {
            Peer2Peer.logger.logDisplay("Error occurred when trying to open a connection with peer "+pId);
        }
        try
        {
            ip=socket.getInputStream();
            op=socket.getOutputStream();
        }
        catch (IOException e) {
            Peer2Peer.logger.logDisplay(this.currentpId+" error occurred when trying to get Data.");
        }
    }
}
