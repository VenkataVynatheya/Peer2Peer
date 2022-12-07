import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Peer2Peer {
    public static HashMap<String, RemotePeerData> hm_peerData = new HashMap<>();
    public static HashMap<String, RemotePeerData> dataPrefNeighbourhashMap = new HashMap<>();
    static Logger logger;
    public static volatile Timer timer;
    public static PayLoadData payLoadCurrent = null;
    static int portNumberClient;
    public static ServerSocket socket = null;
    public static Thread thread;
    public static String ID_peer;
    public static boolean isComplete = false;
    public static Queue<DataParameters> queue = new LinkedList<>();
    public static Vector<Thread> vector = new Vector<>();
    public static Thread mp;
    public static Vector<Thread> peerThread = new Vector<>();
    public static HashMap<String, Socket> pD = new HashMap<>();
    public static volatile Hashtable<String, RemotePeerData> prefNeighbours_HM = new Hashtable<>();
    public static volatile Hashtable<String, RemotePeerData> unchokedNeighbours_HM = new Hashtable<>();

    public static void main(String args[]) throws Exception {
        ID_peer = args[0];
        logger = new Logger("Peer_" + ID_peer + ".log");
        boolean flag = false;
        try {

            logger.logDisplay(ID_peer + " is started");
            obtainDataConfig(); // 1
            System.out.println("1");
            getDateOfPeerdata(); // 2
            assignPrefNeighbours(ID_peer); // no
            System.out.println("2");
            x: for (Map.Entry<String, RemotePeerData> hashMap : hm_peerData.entrySet()) {
                RemotePeerData r = hashMap.getValue();
                if (r.ID_peer.equals(ID_peer)) {
                    portNumberClient = Integer.parseInt(r.portNumber_Peer);

                    if (r.hasFile) {
                        flag = true;
                        break x;
                    }
                }
            }
            payLoadCurrent = new PayLoadData();
            payLoadCurrent.initiatePayload(ID_peer, flag);
            Thread t = new Thread(new DataController(ID_peer));
            t.start();
            System.out.println(flag);
            if (flag) {
                try {
                    Peer2Peer.socket = new ServerSocket(portNumberClient);
                    thread = new Thread(new Server(Peer2Peer.socket, ID_peer));
                    thread.start();
                } catch (Exception ex) {
                    logger.logDisplay(ID_peer + " peer is getting an exception while starting the thread");
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
                    logger.logDisplay(ID_peer + " peer is getting an exception while starting the thread");
                    logger.logExit();
                    System.exit(0);
                }

            }
            System.out.println(Constants.unchokingWindow);
            System.out.println("spr");
            assignPrefNeighbours();

            assignUnchokedNeighbours();
            Thread cT = thread;
            Thread mp = t;
            System.out.println("kl");
            while (true) {
                isComplete = isCompleteFlag();
                System.out.println(isComplete);
                if (isComplete) {
                    logger.logDisplay("All peers have completed downloading the file.");

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
                        System.out.println("3");
                        Thread.sleep(5000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        } catch (Exception exception) {
            logger.logDisplay(String.format(ID_peer + " is ending with error : " + exception.getMessage()));
        } finally {
            logger.logDisplay(String.format(ID_peer + " Peer is terimating."));
            logger.logExit();
            System.exit(0);
        }
    }

    private static void getDateOfPeerdata() throws IOException {
        String configurations;
        BufferedReader b = null;

        try {
            b = new BufferedReader(new FileReader(Constants.PEERS_PATH));
            while ((configurations = b.readLine()) != null) {
                String[] line = configurations.split(" ");
                hm_peerData.put(line[0], new RemotePeerData(line[0], line[1], line[2], line[3].equals("1")));

            }
        } catch (Exception ex) {
            logger.logDisplay(ex.getMessage());
        } finally {
            b.close();
        }
    }

    private static void peerFileCreate() {
        try {
            File f = new File(ID_peer, Constants.fileDesc);
            OutputStream fop = new FileOutputStream(f, true);
            byte currentByte = 0;
            int i = 0;
            while (i < Constants.sizeOfFile) {
                fop.write(currentByte);
                i++;
            }
            fop.close();

        } catch (Exception e) {
            logger.logDisplay("Error while creating intial dummy file for peer " + ID_peer);
        }
    }

    private static void assignPrefNeighbours(String pId) {
        for (Map.Entry<String, RemotePeerData> hm : hm_peerData.entrySet()) {
            if (!hm.getKey().equals(pId)) {
                dataPrefNeighbourhashMap.put(hm.getKey(), hm.getValue());
            }
        }
    }

    public static void obtainDataConfig() throws IOException {
        String configurations;
        BufferedReader b = null;
        try {
            b = new BufferedReader(new FileReader(Constants.COMMON_CONFIG_PATH));
            while ((configurations = b.readLine()) != null) {

                String[] line = configurations.split(" ");
                if (line[0].trim().equals("NumberOfPreferredNeighbors")) {
                    Constants.prefNeighbourCount = Integer.parseInt(line[1]);
                }
                if (line[0].trim().equals("UnchokingInterval")) {
                    Constants.unchokingWindow = Integer.parseInt(line[1]);
                }
                if (line[0].trim().equals("OptimisticUnchokingInterval")) {
                    Constants.favourableUnchokingWindow = Integer.parseInt(line[1]);
                }
                if (line[0].trim().equals("FileName")) {
                    Constants.fileDesc = line[1];
                }
                if (line[0].trim().equals("FileSize")) {
                    Constants.sizeOfFile = Integer.parseInt(line[1]);
                }
                if (line[0].trim().equals("PieceSize")) {
                    Constants.sizeOfbit = Integer.parseInt(line[1]);
                }
            }
        } catch (Exception ex) {
            logger.logDisplay(ex.getMessage());
        } finally {
            b.close();
        }
    }

    public static synchronized DataParameters deleteQueueData() {
        DataParameters dp = null;
        if (queue.isEmpty()) {
        } else {
            dp = queue.remove();
        }
        return dp;
    }

    public static synchronized void appendToQueue(DataParameters dp) {
        queue.add(dp);
    }

    public static class assignPrefNeighbours extends TimerTask {
        public void run() {
            readAdjacentPeerData();
            Enumeration<String> remoteID_peers = Collections.enumeration(hm_peerData.keySet());
            int interestedPeers = 0;
            StringBuilder s = new StringBuilder();
            while (remoteID_peers.hasMoreElements()) {
                String remoteID_peer = remoteID_peers.nextElement();
                RemotePeerData remotepeer = hm_peerData.get(remoteID_peer);
                if (remoteID_peer.equals(ID_peer))
                    continue;
                if (remotepeer.isFinished == 0 && remotepeer.isHandShake == 1)
                    interestedPeers++;
                else if (remotepeer.isFinished == 1) {
                    try {
                        dataPrefNeighbourhashMap.remove(remoteID_peer);
                    } catch (Exception ignored) {
                    }
                }
            }
            if (interestedPeers > Constants.bit) {

                if (!dataPrefNeighbourhashMap.isEmpty())
                    dataPrefNeighbourhashMap.clear();

                List<RemotePeerData> arrayListOfRemotePeers = new ArrayList<>(hm_peerData.values());
                arrayListOfRemotePeers.sort(new RemotePeerData());
                int Total = 0;

                for (RemotePeerData remotePeerData : arrayListOfRemotePeers) {
                    if (Total > Constants.prefNeighbourCount - 1)
                        break;

                    if (remotePeerData.isHandShake == 1 && !remotePeerData.ID_peer.equals(ID_peer)
                            && hm_peerData.get(remotePeerData.ID_peer).isFinished == 0) {
                        hm_peerData.get(remotePeerData.ID_peer).isPrefNeighbour_Peer = 1;
                        dataPrefNeighbourhashMap.put(remotePeerData.ID_peer,
                                hm_peerData.get(remotePeerData.ID_peer));
                        Total++;
                        s.append(remotePeerData.ID_peer).append(", ");

                        if (hm_peerData.get(remotePeerData.ID_peer).peer_isChoked == 1) {
                            requestUnchoke(remotePeerData.ID_peer,
                                    Peer2Peer.pD.get(remotePeerData.ID_peer));
                            Peer2Peer.hm_peerData.get(remotePeerData.ID_peer).peer_isChoked = 0;
                            requestHaveMessage(remotePeerData.ID_peer, Peer2Peer.pD.get(remotePeerData.ID_peer));
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
                            s.append(nextID_peer).append(", ");
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
            if (!s.toString().equals(""))
                logger.logDisplay(Peer2Peer.ID_peer + " has selected the preferred neighbors " + s);
        }
    }

    private static void requestUnchoke(String remoteID_peer, Socket socket) {
        logger.logDisplay(ID_peer + " is sending UNCHOKE message to Peer " + remoteID_peer);

        sendOutput(MessageInfo.array_DataToByte(new MessageInfo(Constants.unChoke)), socket);
    }

    private static void requestHaveMessage(String remoteID_peer, Socket socket) {
        byte[] b = Peer2Peer.payLoadCurrent.data_Encode();
        logger.logDisplay(ID_peer + " is sending HAVE message to Peer " + remoteID_peer);

        sendOutput(MessageInfo.array_DataToByte(new MessageInfo(Constants.have, b)), socket);
    }

    public static void readAdjacentPeerData() {
        try {
            String peerInfo;
            BufferedReader br = new BufferedReader(new FileReader(Constants.PEERS_PATH));
            while ((peerInfo = br.readLine()) != null) {
                String[] p = peerInfo.trim().split(" ");
                String ID_peer = p[0];
                if (Integer.parseInt(p[3]) == 1) {
                    hm_peerData.get(ID_peer).isFinished = 1;
                    hm_peerData.get(ID_peer).isInterested_Peer = 0;
                    hm_peerData.get(ID_peer).peer_isChoked = 0;
                }
            }
            br.close();
        } catch (Exception exception) {
            logger.logDisplay(ID_peer + "" + exception.toString());
        }
    }

    public static synchronized boolean isCompleteFlag() {
        String peerInfo;
        int Total = 1;

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(
                    Constants.PEERS_PATH));
            while ((peerInfo = bufferedReader.readLine()) != null) {
                Total = Total
                        * Integer.parseInt(peerInfo.trim().split(" ")[3]);
            }
            bufferedReader.close();
            return Total != 0;
        } catch (Exception e) {
            logger.logDisplay(e.toString());
            return false;
        }
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

    public static void assignUnchokedNeighbours() {
        timer = new Timer();
        timer.schedule(new assignUnchokedNeighbours(),
                0, Constants.favourableUnchokingWindow * 1000L);
    }

    public static void stopUnchokedNeighbors() {
        timer.cancel();
    }

    public static void assignPrefNeighbours() {
        timer = new Timer();
        timer.schedule(new assignPrefNeighbours(),
                0, Constants.unchokingWindow * 1000L);
    }

    public static void stopPrefNeighbours() {
        timer.cancel();
    }

    private static void sendOutput(byte[] b, Socket socket) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(b);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
