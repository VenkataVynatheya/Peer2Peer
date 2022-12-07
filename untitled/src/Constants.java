public class Constants {
    public static final String PEERS_PATH = "PeerInfo.cfg";
    public static final String COMMON_CONFIG_PATH = "CommonConfig.cfg";
    public static final String zeroBits = "0000000000";
    public static final String handshakeHeader = "P2PFILESHARINGPROJ";
    public static int prefNeighbourCount = 0;
    public static int unchokingWindow = 0;
    public static int favourableUnchokingWindow = 0;
    public static int sizeOfFile = 0;
    public static int sizeOfbit = 0;
    public static String fileDesc = "";
    public static final int maxbitLength = 4;
    public static final int handShakeMessageLength = 32;
    public static final int size_Header = 18;
    public static final int size_ZeroBit = 10;
    public static final int size_PeerID = 4;
    public static final int message_Size = 4;
    public static final int message_Type = 1;
    public static final int choke = 0;
    public static final int unChoke = 1;
    public static final int Interested = 2;
    public static final int notInterested = 3;
    public static final int have = 4;
    public static final int bitField = 5;
    public static final int request = 6;
    public static final int bit = 7;

    public static int byteToIntConverter(byte[] data, int offset) {
        int res = 0;
        for (int j = 0; j < 4; j++) {
            int s = (3 - j) * 8;
            res += (data[j + offset] & 0x000000FF) << s;
        }
        return res;
    }

    public static byte[] intToByteConverter(int val) {
        byte[] b = new byte[4];
        int i = 0;
        while (i < 4) {
            int o = (b.length - 1 - i) * 8;
            b[i] = (byte) ((val >>> o) & (0xFF));
            i++;
        }
        return b;
    }
}