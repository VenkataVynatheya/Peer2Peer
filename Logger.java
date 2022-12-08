import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;


public class Logger {
    static OutputStreamWriter writer;
    static FileOutputStream file;
    

    Logger(String fileName) throws Exception {
        File dir = new File("logs");
        dir.mkdir();
        File logsFile = new File("logs", fileName);
        file = new FileOutputStream("logs//" + fileName);
        writer = new OutputStreamWriter(file, StandardCharsets.UTF_8);
    }

    public void logDisplay(String message) {
        SimpleDateFormat date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Calendar calender = Calendar.getInstance();
        printLog(date.format(calender.getTime()) + " Peer " + message);
        System.out.println(date.format(calender.getTime()) + " Peer " + message);
    }
    
    public void printLog(String s) {
        try {
            writer.write(s + "\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
    }

    public void logExit() {
        try {
            file.close();
            writer.flush();
        } catch (Exception exn) {
            exn.printStackTrace();
            System.out.println(exn.getMessage());
        }
    }
}