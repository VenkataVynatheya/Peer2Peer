import java.io.*;
import java.net.Socket;
import java.util.*;

public class DataController implements Runnable {
    private static String pId = null;
    RandomAccessFile rf;

    public DataController(String pId) {
        DataController.pId = pId;
    }

    public void run() {
        MessageInfo m;
        DataParameters dp;
        String dataType;
        String currentID_peer;

        while (true) {
            dp = Peer2Peer.deleteQueueData();
            while (dp == null) {
                Thread.currentThread();
                try {
                    Thread.sleep(500);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                dp = Peer2Peer.deleteQueueData();
            }

            m = dp.getMessage();

            dataType = m.fetchdata_Type();
            currentID_peer = dp.getpeerID();
            int position = Peer2Peer.hm_peerData.get(currentID_peer).position;
            if (dataType.equals("" + Constants.have) && position != 14) {
                Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " got HAVE message from Peer " + currentID_peer);
                if (dividePayLoadData(currentID_peer, m)) {
                    sendInterestedMessage(currentID_peer, Peer2Peer.pD.get(currentID_peer));
                    Peer2Peer.hm_peerData.get(currentID_peer).position = 9;
                } else {
                    sendNotInterestedMessage(currentID_peer, Peer2Peer.pD.get(currentID_peer));
                    Peer2Peer.hm_peerData.get(currentID_peer).position = 13;
                }
            } else {
                switch (position) {
                    case 2:
                        if (dataType.equals("" + Constants.bitField)) {
                            Peer2Peer.logger
                                    .logDisplay(
                                            Peer2Peer.ID_peer + " got BITFIELD message from Peer " + currentID_peer);
                            sendBitFieldMessage(currentID_peer, Peer2Peer.pD.get(currentID_peer));
                            Peer2Peer.hm_peerData.get(currentID_peer).position = 3;
                        }
                        break;

                    case 3:

                        if (dataType.equals("" + Constants.Interested)) {
                            Peer2Peer.logger
                                    .logDisplay(Peer2Peer.ID_peer + " got a REQUEST message to Peer " + currentID_peer);
                            Peer2Peer.logger
                                    .logDisplay(
                                            Peer2Peer.ID_peer + " got INTERESTED message from Peer " + currentID_peer);
                            Peer2Peer.hm_peerData.get(currentID_peer).isInterested_Peer = 1;
                            Peer2Peer.hm_peerData.get(currentID_peer).isHandShake = 1;

                            if (!Peer2Peer.prefNeighbours_HM.containsKey(currentID_peer)
                                    && !Peer2Peer.unchokedNeighbours_HM.containsKey(currentID_peer)) {
                                sendChokeMessage(currentID_peer, Peer2Peer.pD.get(currentID_peer));
                                Peer2Peer.hm_peerData.get(currentID_peer).peer_isChoked = 1;
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 6;
                            } else {
                                Peer2Peer.hm_peerData.get(currentID_peer).peer_isChoked = 0;
                                sendUnChokeMessage(currentID_peer, Peer2Peer.pD.get(currentID_peer));
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 4;
                            }
                        } else if (dataType.equals("" + Constants.notInterested)) {
                            Peer2Peer.logger.logDisplay(
                                    Peer2Peer.ID_peer + " got NOT INTERESTED message from Peer " + currentID_peer);
                            Peer2Peer.hm_peerData.get(currentID_peer).isInterested_Peer = 0;
                            Peer2Peer.hm_peerData.get(currentID_peer).position = 5;
                            Peer2Peer.hm_peerData.get(currentID_peer).isHandShake = 1;
                        }
                        break;

                    case 4:
                        if (dataType.equals("" + Constants.request)) {
                            dataTransmission(Peer2Peer.pD.get(currentID_peer), m, currentID_peer);
                            if (!Peer2Peer.prefNeighbours_HM.containsKey(currentID_peer)
                                    && !Peer2Peer.unchokedNeighbours_HM.containsKey(currentID_peer)) {
                                sendChokeMessage(currentID_peer, Peer2Peer.pD.get(currentID_peer));
                                Peer2Peer.hm_peerData.get(currentID_peer).peer_isChoked = 1;
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 6;
                            }
                        }
                        break;

                    case 8:
                        if (dataType.equals("" + Constants.bitField)) {
                            if (dividePayLoadData(currentID_peer, m)) {
                                sendInterestedMessage(currentID_peer, Peer2Peer.pD.get(currentID_peer));
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 9;
                            } else {
                                sendNotInterestedMessage(currentID_peer, Peer2Peer.pD.get(currentID_peer));
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 13;
                            }
                        }
                        break;

                    case 9:
                        if (dataType.equals("" + Constants.choke)) {
                            Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " got CHOKED by Peer " + currentID_peer);
                            Peer2Peer.hm_peerData.get(currentID_peer).position = 14;
                        } else if (dataType.equals("" + Constants.unChoke)) {
                            Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " got CHOKED by Peer " + currentID_peer);
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " got UNCHOKED by Peer " + currentID_peer);
                            int initialConflict = Peer2Peer.payLoadCurrent.get_firstBitField(
                                    Peer2Peer.hm_peerData.get(currentID_peer).payloadData);
                            if (initialConflict != -1) {
                                sendRequest(initialConflict, Peer2Peer.pD.get(currentID_peer));
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 11;
                                Peer2Peer.hm_peerData.get(currentID_peer).time1 = new Date();
                            } else
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 13;
                        }
                        break;

                    case 11:
                        if (dataType.equals("" + Constants.choke)) {
                            Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " got CHOKED by Peer " + currentID_peer);
                            Peer2Peer.hm_peerData.get(currentID_peer).position = 14;
                        }

                        else if (dataType.equals("" + Constants.bit)) {
                            byte[] payLoad_Array = m.array_getPayLoad();
                            Peer2Peer.hm_peerData.get(currentID_peer).time2 = new Date();
                            long d = Peer2Peer.hm_peerData.get(currentID_peer).time2.getTime()
                                    - Peer2Peer.hm_peerData.get(currentID_peer).time1.getTime();
                            Peer2Peer.hm_peerData
                                    .get(currentID_peer).rateOfStream = ((double) (payLoad_Array.length
                                            + Constants.message_Size + Constants.message_Type) / (double) d) * 100;
                            Payloadbit p = Payloadbit.convertTobit(payLoad_Array);
                            Peer2Peer.payLoadCurrent.refresh_payLoad(p, "" + currentID_peer);
                            int idx = Peer2Peer.payLoadCurrent.get_firstBitField(
                                    Peer2Peer.hm_peerData.get(currentID_peer).payloadData);
                            if (idx != -1) {
                                sendRequest(idx, Peer2Peer.pD.get(currentID_peer));
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 11;
                                Peer2Peer.hm_peerData.get(currentID_peer).time1 = new Date();
                            } else
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 13;
                            Peer2Peer.readAdjacentPeerData();
                            ;

                            Enumeration<String> keys = Collections
                                    .enumeration(Peer2Peer.hm_peerData.keySet());
                            while (keys.hasMoreElements()) {
                                String nextElement = keys.nextElement();
                                RemotePeerData r = Peer2Peer.hm_peerData.get(nextElement);
                                if (nextElement.equals(Peer2Peer.ID_peer))
                                    continue;
                                if (r.isFinished == 0 && r.peer_isChoked == 0 && r.isHandShake == 1) {
                                    sendHaveMessage(nextElement, Peer2Peer.pD.get(nextElement));
                                    Peer2Peer.hm_peerData.get(nextElement).position = 3;
                                }
                            }
                        }

                        break;

                    case 14:
                        if (dataType.equals("" + Constants.unChoke)) {
                            Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " got CHOKED by Peer " + currentID_peer);
                            try {
                                Thread.sleep(6000);
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                                ;
                            }
                            Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " got UNCHOKED by Peer " + currentID_peer);
                            Peer2Peer.hm_peerData.get(currentID_peer).position = 14;
                        } else if (dataType.equals("" + Constants.have)) {
                            if (dividePayLoadData(currentID_peer, m)) {
                                sendInterestedMessage(currentID_peer, Peer2Peer.pD.get(currentID_peer));
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 9;
                            } else {
                                sendNotInterestedMessage(currentID_peer, Peer2Peer.pD.get(currentID_peer));
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 13;
                            }
                        }

                        break;
                }
            }

        }
    }

    private void dataTransmission(Socket socket, MessageInfo requestMessage, String pId) {
        byte[] bidx = requestMessage.array_getPayLoad();
        int pidx = Constants.byteToIntConverter(bidx, 0);
        byte[] byte_Read = new byte[Constants.sizeOfbit];
        int readBytes = 0;
        File f = new File(Peer2Peer.ID_peer, Constants.fileDesc);

        Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " is sending bit " + pidx + " to Peer " + pId);
        try {
            rf = new RandomAccessFile(f, "r");
            rf.seek((long) pidx * Constants.sizeOfbit);
            readBytes = rf.read(byte_Read, 0, Constants.sizeOfbit);
        } catch (Exception ex) {
            Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " has error in reading the file: " + ex.toString());
        }

        byte[] buffer_Bytes = new byte[readBytes + Constants.maxbitLength];
        System.arraycopy(bidx, 0, buffer_Bytes, 0, Constants.maxbitLength);
        System.arraycopy(byte_Read, 0, buffer_Bytes, Constants.maxbitLength, readBytes);

        sendOutput(MessageInfo.array_DataToByte(new MessageInfo(Constants.bit, buffer_Bytes)), socket);
        try {
            rf.close();
        } catch (Exception ignored) {
        }
    }

    private void sendRequest(int pNo, Socket socket) {
        byte[] p = new byte[Constants.maxbitLength];
        for (int i = 0; i < Constants.maxbitLength; i++)
            p[i] = 0;

        byte[] pidxArray = Constants.intToByteConverter(pNo);
        System.arraycopy(pidxArray, 0, p, 0,
                pidxArray.length);

        sendOutput(MessageInfo.array_DataToByte(new MessageInfo(Constants.request, p)), socket);
    }

    private void sendNotInterestedMessage(String pId, Socket socket) {
        Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " sent a NOT INTERESTED message to Peer " + pId);
        sendOutput(MessageInfo.array_DataToByte(new MessageInfo(Constants.notInterested)), socket);
    }

    private void sendInterestedMessage(String pId, Socket socket) {
        Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " sent a REQUEST message to Peer " + pId);
        Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " sent a INTERESTED message to Peer " + pId);
        sendOutput(MessageInfo.array_DataToByte(new MessageInfo(Constants.Interested)), socket);
    }

    private boolean dividePayLoadData(String pId, MessageInfo md) {
        PayLoadData payloadData = PayLoadData.data_Decode(md.array_getPayLoad());
        Peer2Peer.hm_peerData.get(pId).payloadData = payloadData;
        return Peer2Peer.payLoadCurrent.dividePayLoadData(payloadData);
    }

    private void sendUnChokeMessage(String pId, Socket socket) {
        Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " sent a UNCHOKE message to Peer " + pId);
        sendOutput(MessageInfo.array_DataToByte(new MessageInfo(Constants.unChoke)), socket);
    }

    private void sendChokeMessage(String pId, Socket socket) {
        Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " sent a CHOKE message to Peer " + pId);
        sendOutput(MessageInfo.array_DataToByte(new MessageInfo(Constants.choke)), socket);
    }

    private void sendOutput(byte[] encodedBitField, Socket socket) {
        try {
            OutputStream op = socket.getOutputStream();
            op.write(encodedBitField);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ;
        }
    }

    private void sendHaveMessage(String pId, Socket socket) {
        Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " sent a HAVE message to Peer " + pId);
        sendOutput(MessageInfo.array_DataToByte(
                new MessageInfo(Constants.have, Peer2Peer.payLoadCurrent.data_Encode())), socket);
    }

    private void sendBitFieldMessage(String pId, Socket socket) {
        Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " sent a BITFIELD message to Peer " + pId);
        sendOutput(MessageInfo.array_DataToByte(
                new MessageInfo(+Constants.bitField, Peer2Peer.payLoadCurrent.data_Encode())), socket);
    }

}
