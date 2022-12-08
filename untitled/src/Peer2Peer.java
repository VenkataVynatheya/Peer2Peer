import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Peer2Peer {
    static int portNumberClient;
    public static ServerSocket socket = null;
    public static Thread thread;
    public static String ID_peer;
    public static Vector<Thread> vector = new Vector<>();
    public static Thread mp;
    public static HashMap<String, RemotePeerData> hm_peerData = new HashMap<>();
    public static HashMap<String, RemotePeerData> dataPrefNeighbourhashMap = new HashMap<>();
    public static boolean isComplete = false;
    public static Queue<DataParameters> queue = new LinkedList<>();
    public static Vector<Thread> peerThread = new Vector<>();
    public static HashMap<String, Socket> pD = new HashMap<>();
    public static volatile Hashtable<String, RemotePeerData> prefNeighbours_HM = new Hashtable<>();
    public static volatile Hashtable<String, RemotePeerData> unchokedNeighbours_HM = new Hashtable<>();

    static Logger logger;
    public static volatile Timer timer;
    public static PayLoadData payLoadCurrent = null;

    private static void peerFileCreate() {
        try {
            byte currentByte = 0;
            int ind = 0;
            File file = new File(ID_peer, Constants.fileDesc);
            OutputStream inputOpt = new FileOutputStream(file, true);
            while (ind < Constants.sizeOfFile) {
                inputOpt.write(currentByte);
                ind++;
            }
            inputOpt.close();

        } catch (Exception e) {
            logger.logDisplay("Error while creating intial dummy file for peer " + ID_peer);
        }
    }

    private static void getDateOfPeerdata() throws IOException {
        String configurations;
        BufferedReader buffer = null;

        try {
            buffer = new BufferedReader(new FileReader(Constants.PEERS_PATH));
            while ((configurations = buffer.readLine()) != null) {
                String[] strArr = configurations.split(" ");
                hm_peerData.put(strArr[0], new RemotePeerData(strArr[0], strArr[1], strArr[2], strArr[3].equals("1")));
            }
        } catch (Exception ex) {
            logger.logDisplay(ex.getMessage());
        } finally {
            buffer.close();
        }
    }

    public static void obtainDataConfig() throws IOException {
        BufferedReader buffer = null;
        String configurations;

        try {
            buffer = new BufferedReader(new FileReader(Constants.COMMON_CONFIG_PATH));
            while ((configurations = buffer.readLine()) != null) {
                String[] configArr = configurations.split(" ");

                if (configArr[0].trim().equals("OptimisticUnchokingInterval")) {
                    Constants.favourableUnchokingWindow = Integer.parseInt(configArr[1]);
                }
                if (configArr[0].trim().equals("FileName")) {
                    Constants.fileDesc = configArr[1];
                }
                if (configArr[0].trim().equals("NumberOfPreferredNeighbors")) {
                    Constants.prefNeighbourCount = Integer.parseInt(configArr[1]);
                }
                if (configArr[0].trim().equals("UnchokingInterval")) {
                    Constants.unchokingWindow = Integer.parseInt(configArr[1]);
                }
                if (configArr[0].trim().equals("FileSize")) {
                    Constants.sizeOfFile = Integer.parseInt(configArr[1]);
                }
                if (configArr[0].trim().equals("PieceSize")) {
                    Constants.sizeOfbit = Integer.parseInt(configArr[1]);
                }
            }
        } catch (Exception ex) {
            logger.logDisplay(ex.getMessage());
        } finally {
            buffer.close();
        }
    }

    private static void assignPrefNeighbours(String pId) {
        for (Map.Entry<String, RemotePeerData> hm : hm_peerData.entrySet()) {
            if (!hm.getKey().equals(pId)) {
                dataPrefNeighbourhashMap.put(hm.getKey(), hm.getValue());
            }
        }
    }

    public static class assignPrefNeighbours extends TimerTask {
        public void run() {
            int interestedPeers = 0;
            readAdjacentPeerData();
            Enumeration<String> remoteID_peers = Collections.enumeration(hm_peerData.keySet());
            StringBuilder stringBuild = new StringBuilder();
            while (remoteID_peers.hasMoreElements()) {
                String remoteID_peer = remoteID_peers.nextElement();
                RemotePeerData remotepeer = hm_peerData.get(remoteID_peer);

                if (remoteID_peer.equals(ID_peer))
                    continue;
                if (remotepeer.isFinished == 1) {
                    try {
                        dataPrefNeighbourhashMap.remove(remoteID_peer);
                    } catch (Exception ignored) {
                    }
                }
                if (remotepeer.isFinished == 0 && remotepeer.isHandShake == 1)
                    interestedPeers++;
            }
            if (interestedPeers > Constants.bit) {
                int Total = 0;
                List<RemotePeerData> arrayListOfRemotePeers = new ArrayList<>(hm_peerData.values());

                if (!dataPrefNeighbourhashMap.isEmpty())
                    dataPrefNeighbourhashMap.clear();

                arrayListOfRemotePeers.sort(new RemotePeerData());

                for (RemotePeerData remotePeerData : arrayListOfRemotePeers) {
                    if (Total > Constants.prefNeighbourCount - 1)
                        break;

                    if (remotePeerData.isHandShake == 1 && !remotePeerData.ID_peer.equals(ID_peer)
                            && hm_peerData.get(remotePeerData.ID_peer).isFinished == 0) {
                        Total++;
                        hm_peerData.get(remotePeerData.ID_peer).isPrefNeighbour_Peer = 1;
                        dataPrefNeighbourhashMap.put(remotePeerData.ID_peer,
                                hm_peerData.get(remotePeerData.ID_peer));
                        stringBuild.append(remotePeerData.ID_peer).append(", ");

                        if (hm_peerData.get(remotePeerData.ID_peer).peer_isChoked == 1) {
                            requestUnchoke(remotePeerData.ID_peer,
                                    Peer2Peer.pD.get(remotePeerData.ID_peer));
                            requestHaveMessage(remotePeerData.ID_peer, Peer2Peer.pD.get(remotePeerData.ID_peer));
                            Peer2Peer.hm_peerData.get(remotePeerData.ID_peer).peer_isChoked = 0;
                            Peer2Peer.hm_peerData.get(remotePeerData.ID_peer).position = 3;
                        }
                    }
                }
            } else {
                remoteID_peers = Collections.enumeration(hm_peerData.keySet());
                while (remoteID_peers.hasMoreElements()) {
                    String nextID_peer = remoteID_peers.nextElement();
                    RemotePeerData remotePeer = hm_peerData.get(nextID_peer);
                    if (nextID_peer.equals(ID_peer))
                        continue;

                    if (remotePeer.isFinished == 0 && remotePeer.isHandShake == 1) {
                        if (!dataPrefNeighbourhashMap.containsKey(nextID_peer)) {
                            stringBuild.append(nextID_peer).append(", ");
                            dataPrefNeighbourhashMap.put(nextID_peer, hm_peerData.get(nextID_peer));
                            hm_peerData.get(nextID_peer).isPrefNeighbour_Peer = 1;
                        }
                        if (remotePeer.peer_isChoked == 1) {
                            requestUnchoke(nextID_peer, Peer2Peer.pD.get(nextID_peer));
                            Peer2Peer.hm_peerData.get(nextID_peer).peer_isChoked = 0;
                            requestHaveMessage(nextID_peer, Peer2Peer.pD.get(nextID_peer));
                            Peer2Peer.hm_peerData.get(nextID_peer).position = 3;
                        }
                    }
                }
            }
            if (!stringBuild.toString().equals(""))
                logger.logDisplay(
                        "Peer ID: " + Peer2Peer.ID_peer + " has chose the preferred neighbors -> " + stringBuild);
        }
    }

    public static synchronized DataParameters deleteQueueData() {
        DataParameters dataParams = null;
        if (!queue.isEmpty()) {
            dataParams = queue.remove();
        }
        return dataParams;
    }

    public static synchronized void appendToQueue(DataParameters dataParams) {
        queue.add(dataParams);
    }

    private static void requestHaveMessage(String remoteID_peer, Socket socket) {
        byte[] bytes = Peer2Peer.payLoadCurrent.data_Encode();
        logger.logDisplay("HAVE message sent from Peer: " + ID_peer + " to Peer: " + remoteID_peer);
        sendOutput(MessageInfo.array_DataToByte(new MessageInfo(Constants.have, bytes)), socket);
    }

    private static void requestUnchoke(String remoteID_peer, Socket socket) {
        logger.logDisplay("UNCHOKE message sent from Peer: " + ID_peer + " to Peer " + remoteID_peer);
        sendOutput(MessageInfo.array_DataToByte(new MessageInfo(Constants.unChoke)), socket);
    }

    public static synchronized boolean isCompleteFlag() {
        String peerInfo;
        int Total = 1;

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(
                    Constants.PEERS_PATH));
            while ((peerInfo = bufferedReader.readLine()) != null) {
                Total *= Integer.parseInt(peerInfo.trim().split(" ")[3]);
            }
            bufferedReader.close();
            return Total != 0;
        } catch (Exception e) {
            logger.logDisplay(e.toString());
            return false;
        }
    }

    public static void readAdjacentPeerData() {
        try {
            String peerInfo;
            BufferedReader buffer = new BufferedReader(new FileReader(Constants.PEERS_PATH));
            while ((peerInfo = buffer.readLine()) != null) {
                String[] peerArr = peerInfo.trim().split(" ");
                String ID_peer = peerArr[0];
                if (Integer.parseInt(peerArr[3]) == 1) {
                    hm_peerData.get(ID_peer).isFinished = 1;
                    hm_peerData.get(ID_peer).isInterested_Peer = 0;
                    hm_peerData.get(ID_peer).peer_isChoked = 0;
                }
            }
            buffer.close();
        } catch (Exception exception) {
            logger.logDisplay(ID_peer + "" + exception.toString());
        }
    }

    public static void assignUnchokedNeighbours() {
        timer = new Timer();
        timer.schedule(new assignUnchokedNeighbours(),
                0, Constants.favourableUnchokingWindow * 1000L);
    }

    public static void stopUnchokedNeighbors() {
        timer.cancel();
    }

    public static class assignUnchokedNeighbours extends TimerTask {

        public void run() {
            readAdjacentPeerData();
            if (!unchokedNeighbours_HM.isEmpty())
                unchokedNeighbours_HM.clear();
            Enumeration<String> remoteID_peers = Collections.enumeration(hm_peerData.keySet());
            Vector<RemotePeerData> remotePeerVector = new Vector<>();
            while (remoteID_peers.hasMoreElements()) {
                String key = remoteID_peers.nextElement();
                RemotePeerData remotePeerData = hm_peerData.get(key);
                if (remotePeerData.peer_isChoked == 1
                        && !key.equals(ID_peer)
                        && remotePeerData.isFinished == 0
                        && remotePeerData.isHandShake == 1)
                    remotePeerVector.add(remotePeerData);
            }

            if (remotePeerVector.size() > 0) {
                Collections.shuffle(remotePeerVector);
                RemotePeerData intialPeer = remotePeerVector.firstElement();
                hm_peerData.get(intialPeer.ID_peer).isOptionalUnchokedNeighbour_Peer = 1;
                unchokedNeighbours_HM.put(intialPeer.ID_peer, hm_peerData.get(intialPeer.ID_peer));
                Peer2Peer.logger
                        .logDisplay(
                                Peer2Peer.ID_peer + " has the optimistically unchoked neighbor " + intialPeer.ID_peer);

                if (hm_peerData.get(intialPeer.ID_peer).peer_isChoked == 1) {
                    Peer2Peer.hm_peerData.get(intialPeer.ID_peer).peer_isChoked = 0;
                    requestUnchoke(intialPeer.ID_peer, Peer2Peer.pD.get(intialPeer.ID_peer));
                    requestHaveMessage(intialPeer.ID_peer, Peer2Peer.pD.get(intialPeer.ID_peer));
                    Peer2Peer.hm_peerData.get(intialPeer.ID_peer).position = 3;
                }
            }
        }
    }

    public static void stopPrefNeighbours() {
        timer.cancel();
    }

    public static void assignPrefNeighbours() {
        timer = new Timer();
        timer.schedule(new assignPrefNeighbours(),
                0, Constants.unchokingWindow * 1000L);
    }

    private static void sendOutput(byte[] b, Socket socket) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(b);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String args[]) throws Exception {
        ID_peer = args[0];
        logger = new Logger("Peer_" + ID_peer + ".log");
        boolean flag = false;
        try {

            logger.logDisplay("Started Peer: " + ID_peer);
            obtainDataConfig();
            getDateOfPeerdata();
            assignPrefNeighbours(ID_peer);
            var: for (Map.Entry<String, RemotePeerData> hashMap : hm_peerData.entrySet()) {
                RemotePeerData remotePeerData = hashMap.getValue();
                if (remotePeerData.ID_peer.equals(ID_peer)) {
                    portNumberClient = Integer.parseInt(remotePeerData.portNumber_Peer);

                    if (remotePeerData.hasFile) {
                        flag = true;
                        break var;
                    }
                }
            }
            payLoadCurrent = new PayLoadData();
            payLoadCurrent.initiatePayload(ID_peer, flag);
            Thread t = new Thread(new DataController(ID_peer));
            t.start();
            if (flag) {
                try {
                    Peer2Peer.socket = new ServerSocket(portNumberClient);
                    thread = new Thread(new Server(Peer2Peer.socket, ID_peer));
                    thread.start();
                } catch (Exception ex) {
                    logger.logDisplay("Exception while starting the thread for Peer: " + ID_peer);
                    logger.logExit();
                    System.exit(0);
                }
            } else {
                peerFileCreate();
                for (Map.Entry<String, RemotePeerData> hm : hm_peerData.entrySet()) {
                    RemotePeerData remotePeerData = hm.getValue();
                    if (Integer.parseInt(ID_peer) > Integer.parseInt(hm.getKey())) {
                        PeerController p = new PeerController(remotePeerData.getAddressOfPeer(),
                                Integer.parseInt(remotePeerData.obtainPeerPortNum()), 1, ID_peer);
                        Thread temp = new Thread(p);
                        peerThread.add(temp);
                        temp.start();
                    }
                }
                try {
                    Peer2Peer.socket = new ServerSocket(portNumberClient);
                    thread = new Thread(new Server(Peer2Peer.socket, ID_peer));
                    thread.start();
                } catch (Exception ex) {
                    logger.logDisplay("Exception while starting the thread for Peer: " + ID_peer);
                    logger.logExit();
                    System.exit(0);
                }

            }
            assignPrefNeighbours();
            assignUnchokedNeighbours();

            Thread cT = thread;
            Thread mp = t;

            while (true) {
                isComplete = isCompleteFlag();
                if (isComplete) {
                    logger.logDisplay("All peers have successfully completed downloading the file.");

                    stopPrefNeighbours();
                    stopUnchokedNeighbors();

                    try {
                        Thread.currentThread();
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {
                    }

                    if (cT.isAlive())
                        cT.stop();

                    if (mp.isAlive())
                        mp.stop();

                    for (Thread thread : peerThread)
                        if (thread.isAlive())
                            thread.stop();

                    for (Thread thread : vector)
                        if (thread.isAlive())
                            thread.stop();
                    break;
                } else {
                    try {
                        Thread.currentThread();
                        Thread.sleep(5000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        } catch (Exception exception) {
            logger.logDisplay(String.format("Error : " + exception.getMessage() + " from Peer ID: " + ID_peer));
        } finally {
            logger.logDisplay(String.format("Completed! Peer ID " + ID_peer + " is terminating."));
            logger.logExit();
            System.exit(0);
        }
    }

}
