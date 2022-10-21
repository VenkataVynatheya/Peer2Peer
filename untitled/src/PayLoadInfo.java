import java.io.*;

public class PayLoadInfo {
    public PayloadPiece[] payloadPiece;
    public int bufferSize;

    PayLoadInfo() {
        double consts = (double) Constants.fileSize / Constants.pieceSize;
        this.bufferSize = (int) Math.ceil(consts);
        this.payloadPiece = new PayloadPiece[this.bufferSize];
        int index = 0;
        while (index < this.bufferSize) {
            this.payloadPiece[index++] = new PayloadPiece();
        }
    }

    public byte[] payloadDataEncode() {
        int size = 0;
        if (this.bufferSize % 8 != 0) {
            size += 1;
        }
        size += this.bufferSize / 8;
        byte[] buffer = new byte[size];
        int var = 0;
        int bufferIndex = 0;
        int index = 1;
        while (index <= this.bufferSize) {
            int piece = this.payloadPiece[index - 1].pieceExists;
            var = var << 1;
            if (piece == 1) {
                var++;
            }
            if (index % 8 == 0) {
                buffer[bufferIndex] = (byte) var;
                bufferIndex++;
                var = 0;
            }
            index++;
        }
        index--;
        if (index % 8 != 0) {
            int bufferShift = this.bufferSize - (this.bufferSize / 8) * 8;
            var <<= (8 - bufferShift);
            buffer[bufferIndex] = (byte) var;
        }
        return buffer;
    }

    public void initiatePayload(String peerID, boolean fileExists) {
        int index = 0;
        if (fileExists) {
            while (index < bufferSize) {
                this.payloadPiece[index].setPieceExists(1);
                this.payloadPiece[index].setSenderPeerID(peerID);
                index++;
            }

        } else {
            while (index < bufferSize) {
                this.payloadPiece[index].setPieceExists(0);
                this.payloadPiece[index].setSenderPeerID(peerID);
                index++;
            }
        }

    }

    class PayloadPiece {
        public int pieceExists;
        public String senderPeerID;

        public void setPieceExists(int payloadPiece) {
            this.pieceExists = payloadPiece;
        }

        public void setSenderPeerID(String senderPeerID) {
            this.senderPeerID = senderPeerID;
        }

    }
}