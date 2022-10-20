import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Peer2Peer {
    public static HashMap<String, RemotePeerData> peerdatahashMap = new HashMap<>();
    public static HashMap<String, RemotePeerData> dataPrefNeighbourhashMap = new HashMap<>();
    static Logger logger;
    public static volatile Timer timer;
    public static PayLoadData payLoadCurrent = null;
    static int portNumberClient;
    public static ServerSocket socket = null;
    public static Thread thread;
    public static String ID_peer;
    public static boolean isCompleteFlag = false;
    public static Queue<DataParams> queue = new LinkedList<>();
    public static Vector<Thread> nv = new Vector<>();
    public static Thread mp;
    public static Vector<Thread> peerThread = new Vector<>();

    public static void main(String args[]) throws Exception {
        ID_peer = args[0];
        logger = new Logger("Peer_" + ID_peer + ".log");
        boolean flag = false;
        try {

            logger.logDisplay(ID_peer + " is started");
            obtainConfigData(); // 1
            getDateOfPeerdata(); // 2

            x: for (Map.Entry<String, RemotePeerData> hashMap : peerdatahashMap.entrySet()) {
                RemotePeerData r = hashMap.getValue();
                if (r.ID_Peer.equals(ID_peer)) {
                    portNumberClient = Integer.parseInt(r.peerPort);

                    if (r.fileExists) {
                        flag = true;
                        break x;
                    }
                }
            }
            payLoadCurrent = new PayLoadData();
            payLoadCurrent.startPayLoad(ID_peer, flag);

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
                peerFileCreation();
                for (Map.Entry<String, RemotePeerData> hashMap : peerdatahashMap.entrySet()) {
                    RemotePeerData RemotePeerData = hashMap.getValue();
                    if (Integer.parseInt(ID_peer) > Integer.parseInt(hashMap.getKey())) {
                        PeerController p = new PeerController(RemotePeerData.getPeerAddress(),
                                Integer.parseInt(RemotePeerData.getPeerPort()), 1, ID_peer);
                        Thread temp = new Thread((Runnable) p);
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

            Thread cT = thread;

            while (true) {
                isCompleteFlag = isDoneFlag();
                if (isCompleteFlag) {
                    logger.logDisplay("All peers have completed downloading the file.");

                    stopPrefNeighbors();

                    try {
                        Thread.currentThread();
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {
                    }

                    if (cT.isAlive())
                        cT.interrupt();

                    if (mp.isAlive())
                        mp.interrupt();

                    for (Thread thread : peerThread)
                        if (thread.isAlive())
                            thread.interrupt();

                    for (Thread thread : nv)
                        if (thread.isAlive())
                            thread.interrupt();

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
            logger.logDisplay(String.format(ID_peer + " is ending with error : " + exception.getMessage()));
        } finally {
            logger.logDisplay(String.format(ID_peer + " Peer is terminating."));
            logger.logExit();
            System.exit(0);
        }
    }

    private static void getDateOfPeerdata() throws IOException {
        String configurations;
        BufferedReader b = null;

        try {
            b = new BufferedReader(new FileReader(Constants.PEERS_INFO_PATH));
            while ((configurations = b.readLine()) != null) {
                String[] line = configurations.split(" ");
                peerdatahashMap.put(line[0], new RemotePeerData(line[0], line[1], line[2], line[3].equals("1")));

            }
        } catch (Exception ex) {
            logger.logDisplay(ex.getMessage());
        } finally {
            b.close();
        }
    }

    private static void peerFileCreation() {
        try {
            File f = new File(ID_peer, Constants.fileDesc);
            OutputStream os = new FileOutputStream(f, true);
            byte intialByte = 0;
            int i = 0;
            while (i < Constants.fileSize) {
                os.write(intialByte);
                i++;
            }
            os.close();

        } catch (Exception e) {
            logger.logDisplay("Error while creating initial dummy file for peer " + ID_peer);
        }
    }

    public static void obtainConfigData() throws IOException {
        String configurations;
        BufferedReader b = null;
        try {
            b = new BufferedReader(new FileReader(Constants.COMMON_CONFIG_PATH));
            while ((configurations = b.readLine()) != null) {

                String[] line = configurations.split(" ");
                if (line[0].trim().equals("PreferredNeighboursTotal")) {
                    Constants.preferredNeighboursTotal = Integer.parseInt(line[1]);
                }

                if (line[0].trim().equals("FileName")) {
                    Constants.fileDesc = line[1];
                }
                if (line[0].trim().equals("FileSize")) {
                    Constants.fileSize = Integer.parseInt(line[1]);
                }
                if (line[0].trim().equals("PieceSize")) {
                    Constants.pieceSize = Integer.parseInt(line[1]);
                }
            }
        } catch (Exception ex) {
            logger.logDisplay(ex.getMessage());
        } finally {
            b.close();
        }
    }

    public static class SetPreferredNeighbours extends TimerTask {
        public void run() {
            Enumeration<String> remoteID_peers = Collections.enumeration(peerdatahashMap.keySet());
            int interestedPeers = 0;
            StringBuilder s = new StringBuilder();
            while (remoteID_peers.hasMoreElements()) {
                String remoteID_peer = remoteID_peers.nextElement();
                RemotePeerData rm = peerdatahashMap.get(remoteID_peer);
                if (remoteID_peer.equals(ID_peer))
                    continue;
                if (rm.isCompleted == 0 && rm.isHandShake == 1)
                    interestedPeers++;
                else if (rm.isCompleted == 1) {
                    try {
                        dataPrefNeighbourhashMap.remove(remoteID_peer);
                    } catch (Exception ignored) {
                    }
                }
            }
            if (interestedPeers > Constants.piece) {

                if (!dataPrefNeighbourhashMap.isEmpty())
                    dataPrefNeighbourhashMap.clear();

                List<RemotePeerData> remotePeersarrayList = new ArrayList<>(peerdatahashMap.values());
                remotePeersarrayList.sort(new RemotePeerData());
                int Total = 0;

                for (RemotePeerData RemotePeerData : remotePeersarrayList) {
                    if (Total > Constants.preferredNeighboursTotal - 1)
                        break;

                    if (RemotePeerData.isHandShake == 1 && !RemotePeerData.ID_Peer.equals(ID_peer)
                            && peerdatahashMap.get(RemotePeerData.ID_Peer).isCompleted == 0) {
                        peerdatahashMap.get(RemotePeerData.ID_Peer).isPreferredNeighbor = 1;
                        dataPrefNeighbourhashMap.put(RemotePeerData.ID_Peer,
                                peerdatahashMap.get(RemotePeerData.ID_Peer));
                        Total++;
                        s.append(RemotePeerData.ID_Peer).append(", ");

                    }
                }
            } else {
                remoteID_peers = Collections.enumeration(peerdatahashMap.keySet());
                while (remoteID_peers.hasMoreElements()) {
                    String nextID_peer = remoteID_peers.nextElement();
                    RemotePeerData remotePeer = peerdatahashMap.get(nextID_peer);
                    if (nextID_peer.equals(ID_peer))
                        continue;

                    if (remotePeer.isCompleted == 0 && remotePeer.isHandShake == 1) {
                        if (!dataPrefNeighbourhashMap.containsKey(nextID_peer)) {
                            s.append(nextID_peer).append(", ");
                            dataPrefNeighbourhashMap.put(nextID_peer, peerdatahashMap.get(nextID_peer));
                            peerdatahashMap.get(nextID_peer).isPreferredNeighbor = 1;
                        }

                    }
                }
            }
            if (!s.toString().equals(""))
                logger.logDisplay(Peer2Peer.ID_peer + " has selected the preferred neighbors " + s);
        }
    }

    public static synchronized boolean isDoneFlag() {
        String infoPeer;
        int Total = 1;

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(
                    Constants.PEERS_INFO_PATH));
            while ((infoPeer = bufferedReader.readLine()) != null) {
                Total = Total
                        * Integer.parseInt(infoPeer.trim().split(" ")[3]);
            }
            bufferedReader.close();
            return Total != 0;
        } catch (Exception e) {
            logger.logDisplay(e.toString());
            return false;
        }
    }

    public static void setPreferredNeighbors() {
        timer = new Timer();
        timer.schedule(new SetPreferredNeighbours(), 0);

    }

    public static void stopPrefNeighbors() {
        timer.cancel();
    }
}
