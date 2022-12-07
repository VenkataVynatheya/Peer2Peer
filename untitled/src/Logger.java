import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;

<<<<<<< HEAD
public class Logger {
    static FileOutputStream file;
    static OutputStreamWriter writer;

    Logger(String fileName) throws Exception {
        File dir = new File("logs");
        dir.mkdir();
        File logsFile = new File("logs", fileName);
        file = new FileOutputStream("logs//" + fileName);
        writer = new OutputStreamWriter(file, StandardCharsets.UTF_8);
    }

    public void printLog(String s) {
        try {
            writer.write(s + "\n");
        } catch (Exception ex) {
=======
public class Logger
{
    static FileOutputStream file;
    static OutputStreamWriter writer;
    Logger(String fileName) throws Exception
    {
        File dir = new File("logs");
        dir.mkdir();
        File logsFile = new File("logs", fileName);
        file=new FileOutputStream("logs//"+fileName);
        writer=new OutputStreamWriter(file, StandardCharsets.UTF_8);
    }
    public void printLog(String s)
    {
        try
        {
            writer.write(s+"\n");
        }
        catch (Exception ex)
        {
>>>>>>> 7a2324b (Added Handshake and Starting remote peers)
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
    }
<<<<<<< HEAD

    public void logExit() {
        try {
            writer.flush();
            file.close();
        } catch (Exception ex) {
=======
    public  void logExit()
    {
        try
        {
            writer.flush();
            file.close();
        }
        catch (Exception ex)
        {
>>>>>>> 7a2324b (Added Handshake and Starting remote peers)
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
    }
<<<<<<< HEAD

    public void logDisplay(String message) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat d = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        printLog(d.format(c.getTime()) + " Peer " + message);
        System.out.println(d.format(c.getTime()) + " Peer " + message);
=======
    public  void logDisplay(String message)
    {
        Calendar c=Calendar.getInstance();
        SimpleDateFormat d=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        printLog(d.format(c.getTime())+" Peer "+ message);
        System.out.println(d.format(c.getTime())+" Peer "+ message);
>>>>>>> 7a2324b (Added Handshake and Starting remote peers)
    }
}
