import java.nio.charset.StandardCharsets;

public class MessageData {

    private byte[] data;
    private byte[] dataSizeArray;
    private byte[] payloadData;
    private String dataLength;
    private String dataType;
    private int lengthOfMessage = Constants.typeOfMessage;

    MessageData() {

    }

    MessageData(int n, byte[] arr) {
        try {
            if (arr == null) {
                if ((n == Constants.choke) || (n == Constants.unChoke) || (n == Constants.intersted)
                        || (n == Constants.notInterested)) {
                    this.lengthOfMessage = 1;
                    this.dataLength = this.lengthOfMessage + "";
                    this.dataSizeArray = Constants.convertIntToByte(this.lengthOfMessage);
                    this.payloadData = null;
                } else {
                    System.out.println("Empty PayLoad");
                }

            } else {
                this.lengthOfMessage = arr.length + 1;
                this.dataLength = this.lengthOfMessage + "";
                this.dataSizeArray = Constants.convertIntToByte(this.lengthOfMessage);
                if (this.dataSizeArray.length > Constants.sizeOfMessage) {
                    System.out.println("Message length greater than size required");
                }
                this.payloadData = arr;
            }
            this.setDataType("" + n);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public byte[] getDataLengthArray() {
        return dataSizeArray;
    }

    public byte[] getDataArray() {
        return data;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(byte[] data) {
        this.dataType = new String(data, StandardCharsets.UTF_8);
        this.data = data;
    }

    public void setDataType(String data) {
        this.dataType = data.trim();
        this.data = this.dataType.getBytes(StandardCharsets.UTF_8);
    }

    public void setDataLength(byte[] b) {
        int l = Constants.convertByteArrayToInt(b, 0);
        this.dataLength = "" + l;
        this.dataSizeArray = b;
        this.lengthOfMessage = l;
    }

    public byte[] getPayloadData() {
        return payloadData;
    }

    public void setPayloadData(byte[] payloadData) {
        this.payloadData = payloadData;
    }

    public static byte[] convertDataToByteArray(MessageData m) {
        byte[] dataByteArray;
        int dType;
        try {
            dType = Integer.parseInt(m.getDataType());
            if ((m.getDataArray() == null) || ((dType < 0) || dType > 7)
                    || (m.getDataLengthArray().length > Constants.sizeOfMessage) || (m.getDataLengthArray() == null)) {
                throw new Exception("Message is Not Valid");
            }
            if (m.getPayloadData() == null) {
                dataByteArray = new byte[Constants.sizeOfMessage + Constants.typeOfMessage];
                System.arraycopy(m.getDataLengthArray(), 0, dataByteArray, 0, m.getDataLengthArray().length);
                System.arraycopy(m.getDataArray(), 0, dataByteArray, Constants.sizeOfMessage, Constants.typeOfMessage);
            } else {
                dataByteArray = new byte[Constants.sizeOfMessage + Constants.typeOfMessage
                        + m.getPayloadData().length];
                System.arraycopy(m.getDataLengthArray(), 0, dataByteArray, 0, m.getDataLengthArray().length);
                System.arraycopy(m.getDataArray(), 0, dataByteArray, Constants.sizeOfMessage, Constants.typeOfMessage);
                System.arraycopy(m.getPayloadData(), 0, dataByteArray,
                        Constants.sizeOfMessage + Constants.typeOfMessage, m.getPayloadData().length);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            dataByteArray = null;
        }
        return dataByteArray;
    }
}
