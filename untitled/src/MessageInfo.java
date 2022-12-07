import java.nio.charset.StandardCharsets;

public class MessageInfo {

    private byte[] array_Info;
    private byte[] array_dataSize;
    private byte[] payLoad_Array;
    private String data_Size;
    private String data_Type;
    private int size_Message = Constants.message_Type;

    MessageInfo() {

    }

    MessageInfo(int n) {
        try {
            if ((n == Constants.choke) || (n == Constants.unChoke) || (n == Constants.Interested)
                    || (n == Constants.notInterested)) {
                this.setdata_Type("" + n);
                this.payLoad_Array = null;
                this.size_Message = 1;
                this.data_Size = this.size_Message + "";
                this.array_dataSize = Constants.intToByteConverter(this.size_Message);
            } else {
                System.out.println("Invalid Message");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    MessageInfo(int n, byte[] arr) {
        try {
            if (arr == null) {
                if ((n == Constants.choke) || (n == Constants.unChoke) || (n == Constants.Interested)
                        || (n == Constants.notInterested)) {
                    this.size_Message = 1;
                    this.data_Size = this.size_Message + "";
                    this.array_dataSize = Constants.intToByteConverter(this.size_Message);
                    this.payLoad_Array = null;
                } else {
                    System.out.println("Empty PayLoad");
                }

            } else {
                this.size_Message = arr.length + 1;
                this.data_Size = this.size_Message + "";
                this.array_dataSize = Constants.intToByteConverter(this.size_Message);
                if (this.array_dataSize.length > Constants.message_Size) {
                    System.out.println("Message length greater than size required");
                }
                this.payLoad_Array = arr;
            }
            this.setdata_Type("" + n);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public int getsize_Message() {
        return size_Message;
    }

    public void setsize_Message(int size_Message) {
        this.size_Message = size_Message;
    }

    public byte[] getarray_dataSize() {
        return array_dataSize;
    }

    public void setarray_dataSize(byte[] array_dataSize) {
        this.array_dataSize = array_dataSize;
    }

    public byte[] getarray_Info() {
        return array_Info;
    }

    public void setarray_Info(byte[] array_Info) {
        this.array_Info = array_Info;
    }

    public String fetchdata_Type() {
        return data_Type;
    }

    public void setdata_Type(byte[] data) {
        this.data_Type = new String(data, StandardCharsets.UTF_8);
        this.array_Info = data;
    }

    public void setdata_Type(String data) {
        this.data_Type = data.trim();
        this.array_Info = this.data_Type.getBytes(StandardCharsets.UTF_8);
    }

    public String getdata_Size() {
        return data_Size;
    }

    public void setdata_Size(byte[] b) {
        int l = Constants.byteToIntConverter(b, 0);
        this.data_Size = "" + l;
        this.array_dataSize = b;
        this.size_Message = l;
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

    public static byte[] array_DataToByte(MessageInfo m) {
        byte[] array_DataByte;
        int dataType;
        try {
            dataType = Integer.parseInt(m.fetchdata_Type());
            if ((m.getarray_Info() == null) || ((dataType < 0) || dataType > 7)
                    || (m.getarray_dataSize().length > Constants.message_Size) || (m.getarray_dataSize() == null)) {
                throw new Exception("Message is Not Valid");
            }
            if (m.array_getPayLoad() == null) {
                array_DataByte = new byte[Constants.message_Size + Constants.message_Type];
                System.arraycopy(m.getarray_dataSize(), 0, array_DataByte, 0, m.getarray_dataSize().length);
                System.arraycopy(m.getarray_Info(), 0, array_DataByte, Constants.message_Size, Constants.message_Type);
            } else {
                array_DataByte = new byte[Constants.message_Size + Constants.message_Type
                        + m.array_getPayLoad().length];
                System.arraycopy(m.getarray_dataSize(), 0, array_DataByte, 0, m.getarray_dataSize().length);
                System.arraycopy(m.getarray_Info(), 0, array_DataByte, Constants.message_Size, Constants.message_Type);
                System.arraycopy(m.array_getPayLoad(), 0, array_DataByte,
                        Constants.message_Size + Constants.message_Type, m.array_getPayLoad().length);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            array_DataByte = null;
        }
        return array_DataByte;
    }

    public static MessageInfo array_ByteToData(byte[] b) {
        MessageInfo m = new MessageInfo();
        byte[] dataLength = new byte[Constants.message_Size];
        byte[] dataType = new byte[Constants.message_Type];
        byte[] payload;
        int mlen;
        try {
            if (b.length < Constants.message_Size + Constants.message_Type || b == null) {
                throw new Exception("Invalid Message");
            }
            System.arraycopy(b, 0, dataLength, 0, Constants.message_Size);
            System.arraycopy(b, Constants.message_Size, dataType, 0, Constants.message_Type);
            m.setdata_Size(dataLength);
            m.setdata_Type(dataType);
            mlen = Constants.byteToIntConverter(dataLength, 0);
            if (mlen > 1) {
                payload = new byte[mlen - 1];
                System.arraycopy(b, Constants.message_Size + Constants.message_Type, payload, 0,
                        b.length - Constants.message_Size - Constants.message_Type);
                m.array_assignPayLoad(payload);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());

        }
        return m;
    }
}
