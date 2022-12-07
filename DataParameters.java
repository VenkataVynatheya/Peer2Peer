public class DataParameters {
  public MessageInfo getMessage() {
      return msginfo;
  }

  public void setMessage(MessageInfo msg) {
      this.msginfo = msg;
  }

  public String getpeerID() {
      return ID_Peer;
  }

  public void setpeerID(String ID_Peer) {
      this.ID_Peer = ID_Peer;
  }

  MessageInfo msginfo;
  String ID_Peer;

  DataParameters() {
      msginfo = new MessageInfo();
      ID_Peer = null;
  }

}