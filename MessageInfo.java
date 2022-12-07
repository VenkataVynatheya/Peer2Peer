import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class MessageInfo {

    
    private String data_Size;
    private String data_Type;
    private byte[] array_Info;
    private byte[] array_dataSize;
    private byte[] payLoad_Array;
    private int size_Message = Constants.messageType;

    MessageInfo() {

    }

    MessageInfo(int n) {
        try {
            if ((n == Constants.choke) || (n == Constants.unChoke) || (n == Constants.Interested)
                    || (n == Constants.notInterested)) {
                this.payLoad_Array = null;
                this.size_Message = 1;
                this.setdata_Type("" + n);
                this.data_Size = this.size_Message + "";
                this.array_dataSize = Constants.intToByteConverter(this.size_Message);
            } else {
                System.out.println("Invalid ");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    MessageInfo(int n, byte[] arr) {
        try {
            if (arr != null) {
                this.size_Message = arr.length + 1;
                this.data_Size = this.size_Message + "";
                this.array_dataSize = Constants.intToByteConverter(this.size_Message);
                if (this.array_dataSize.length > Constants.messageSize) {
                    System.out.println("Message length greater than size required");
                }
                this.payLoad_Array = arr;
                

            } else {
              if ((n == Constants.choke) || (n == Constants.unChoke) || (n == Constants.Interested)
              || (n == Constants.notInterested)) {
                this.size_Message = 1;
                this.data_Size = this.size_Message + "";
                this.array_dataSize = Constants.intToByteConverter(this.size_Message);
                this.payLoad_Array = null;
      } else {
          System.out.println("Empty PayLoad");
      }
            }
            this.setdata_Type("" + n);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    
   
    public int getsize_Message() {
        return size_Message;
    }
    public byte[] getarray_Info() {
      return array_Info;
    }
    public String getdata_Size() {
      return data_Size;
    }
    public byte[] getarray_dataSize() {
      return array_dataSize;
    }

    public void setsize_Message(int size_Message) {
        this.size_Message = size_Message;
    }
    public void setarray_dataSize(byte[] array_dataSize) {
        this.array_dataSize = array_dataSize;
    }
    public void setarray_Info(byte[] array_Info) {
        this.array_Info = array_Info;
    }
    public String fetchdata_Type() {
        return data_Type;
    }
    public void setdata_Size(byte[] b) {
      int l = Constants.byteToIntConverter(b, 0);
      this.data_Size = "" + l;
      this.array_dataSize = b;
      this.size_Message = l;
  }    
    public void setdata_Type(byte[] data) {
        this.data_Type = new String(data, StandardCharsets.UTF_8);
        this.array_Info = data;
    }
    public void setdata_Type(String data) {
        this.data_Type = data.trim();
        this.array_Info = this.data_Type.getBytes(StandardCharsets.UTF_8);
    }
  
    public void setdata_Size(String data) {
        this.size_Message = Integer.parseInt(data);
        this.data_Size = data;
        this.array_dataSize = Constants.intToByteConverter(this.size_Message);
    }

    public byte[] array_getPayLoad() {
        return payLoad_Array;
    }

    public void array_assignPayLoad(byte[] payLoad_Array) {
        this.payLoad_Array = payLoad_Array;
    }

    public static byte[] array_DataToByte(MessageInfo msg) {
        byte[] array_DataByte;
        int dataType;
        try {
            dataType = Integer.parseInt(msg.fetchdata_Type());
            if ((msg.getarray_Info() == null) || ((dataType < 0) || dataType > 7)
                    || (msg.getarray_dataSize().length > Constants.messageSize) || (msg.getarray_dataSize() == null)) {
                throw new Exception("Message Invalid");
            }
            if (msg.array_getPayLoad() == null) {
                array_DataByte = new byte[Constants.messageSize + Constants.messageType];
                System.arraycopy(msg.getarray_dataSize(), 0, array_DataByte, 0, msg.getarray_dataSize().length);
                System.arraycopy(msg.getarray_Info(), 0, array_DataByte, Constants.messageSize, Constants.messageType);
            } else {
                array_DataByte = new byte[Constants.messageSize + Constants.messageType
                        + msg.array_getPayLoad().length];
                System.arraycopy(msg.getarray_dataSize(), 0, array_DataByte, 0, msg.getarray_dataSize().length);
                System.arraycopy(msg.getarray_Info(), 0, array_DataByte, Constants.messageSize, Constants.messageType);
                System.arraycopy(msg.array_getPayLoad(), 0, array_DataByte,
                        Constants.messageSize + Constants.messageType, msg.array_getPayLoad().length);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            array_DataByte = null;
        }
        return array_DataByte;
    }

    public static MessageInfo array_ByteToData(byte[] b) {
        MessageInfo msg = new MessageInfo();
        byte[] dataType = new byte[Constants.messageType];
        byte[] payload;
        byte[] dataLength = new byte[Constants.messageSize];
        
        int msglen;
        try {
            if (b.length < Constants.messageSize + Constants.messageType || b == null) {
                throw new Exception(" Message Invalid ");
            }
            System.arraycopy(b, 0, dataLength, 0, Constants.messageSize);
            System.arraycopy(b, Constants.messageSize, dataType, 0, Constants.messageType);
            msg.setdata_Size(dataLength);
            msg.setdata_Type(dataType);
            msglen = Constants.byteToIntConverter(dataLength, 0);
            if (msglen > 1) {
                payload = new byte[msglen - 1];
                System.arraycopy(b, Constants.messageSize + Constants.messageType, payload, 0,
                        b.length - Constants.messageSize - Constants.messageType);
                msg.array_assignPayLoad(payload);
            }
        } catch (Exception exn) {
            System.out.println(exn.getMessage());

        }
        return msg;
    }
}