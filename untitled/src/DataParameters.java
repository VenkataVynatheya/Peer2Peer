public class DataParameters {
    MessageInfo msginfo;
    String ID_Peer;

    DataParameters() {
        msginfo = new MessageInfo();
        ID_Peer = null;
    }

    public String getpeerID() {
        return ID_Peer;
    }

    public void setpeer_ID(String ID_Peer) {
        this.ID_Peer = ID_Peer;
    }

    public MessageInfo getMessage() {
        return msginfo;
    }

    public void setMessage(MessageInfo m) {
        this.msginfo = m;
    }
}