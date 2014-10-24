import java.net.SocketException;
import java.util.*;

/**
 * The API given by instructor
 * Name: Chenguang He
 * Email: readman@iastate.edu
 * Created by chenguanghe on 9/17/14.
 */
public class rdtServices {
    private static Random generator = new Random(System.currentTimeMillis());
    static HashMap<Integer, Pair> pairs = new HashMap<Integer, Pair>();
    static int numOfPairs = 1;
    static Queue<char[]> queue = new LinkedList<char[]>();
    static int numberofPacket = 1;

    private rdtServices() {
    }

    /**
     * initialize the system.
     */
    public static void SystemInit() {
    }

    /**
     * create a pair of sender and receiver.
     * The packet loss rate and packet corrupt rate (among non-lost packets) are specified by the arguments
     * lossRate and corruptRate passed in.
     * Argument windowSize specifies the total number of unacknowledged packets that are allowed.
     * Argument timeOut specifies the timer firing interval used in rdt 3.0 protocol.
     * A unique pair identity is returned
     *
     * @param lossRate    the rate of loss
     * @param corruptRate the rate of corrupt
     * @param windowSize  the size of windows
     * @param timeOut     the time of timeout
     * @return the pair of id.
     */
    public static int AddParties(float lossRate, float corruptRate, int windowSize, int timeOut) {
        int recPort = generator.nextInt(1000) + 10203;
        int sendPort = generator.nextInt(1000) + 15502;
        while (recPort == sendPort) {
            sendPort = generator.nextInt(1000) + 15401;
        }
        try {
            Sender sender = new Sender(windowSize, timeOut, recPort, sendPort);
            Receiver receiver = new Receiver(lossRate, corruptRate, windowSize, sendPort, recPort);
            Pair pair = new Pair(sender, receiver, sendPort, recPort);
            while (pairs.containsValue(pair)) {
                recPort = generator.nextInt(1000) + 13040;
                sendPort = generator.nextInt(1000) + 15040;
                while (recPort == sendPort) {
                    sendPort = generator.nextInt(1000) + 12050;
                }
                sender = new Sender(windowSize, timeOut, recPort, sendPort);
                receiver = new Receiver(lossRate, corruptRate, windowSize, sendPort, recPort);
                pair = new Pair(sender, receiver, sendPort, recPort);
            }
            pairs.put(numOfPairs, pair);
            final Receiver finalReceiver = receiver;
            new Thread() {
                public void run() {
                    try {
                        finalReceiver.Receive();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            final Sender finalSender = sender;
            new Thread() {
                public void run() {
                    try {
                        finalSender.SendData();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            numOfPairs++;
            return numOfPairs - 1;
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * the sender of pair id sends a packet,
     * of which the payload is stored in buf with length len,
     * is sent to the receiver of pair id. If the sending operation is blocking
     * (i.e., it returns only after the packet has successfully arrived at the receiver)
     * isBlocked is set to be greater than 0;
     * otherwise, the operation is non-blocking.
     * The maximum length of payload is limited to 64 bytes.
     *
     * @param id        the id of pair
     * @param buf       the buffer
     * @param len       the size of buffer
     * @param isBlocked larger than 0, if blocked, otherwise 0
     */
    public static void RdtSend(int id, char buf[], int len, int isBlocked) throws Exception {
        Pair pair = pairs.get(id);
        if (isBlocked > 0)
            queue.offer(buf);
        else {
            queue.offer(buf);
            while (!queue.isEmpty()) {
                numberofPacket++;
                char[] chars = queue.poll();
                pair.sender.pushToQueue(chars, chars.length);
            }
        }
    }

    /**
     * returns the statistics about the most recently-sent m packets within a pair.
     * For each packet, show
     * (i) whether the packet has already arrived at the receiver;
     * (ii) how many retransmission has been caused;
     * and
     * (iii) the reason (packet loss, packet corruption, ACK loss, or ACK corruption) for each re-transmission.
     *
     * @param pairId the id of pair
     * @param m      most recently-sent m packets
     */
    public static void RdtStats(int pairId, int m) throws InterruptedException {
        Pair p = pairs.get(pairId);
        while (p.isBlocked()) {
            Thread.sleep(100);
        }
        Sender sender = p.sender;
        Receiver receiver = p.receiver;
        HashMap<Integer, ArrayList<String>> senderLog = sender.log;
        HashMap<Integer, ArrayList<String>> receiverLog = receiver.log;
        int end = Math.max(senderLog.size(), receiverLog.size());
        int tmp = end - m;
        System.out.println();
        System.out.println("---------------------------------- PairId: " + pairId + " -------------------------------------------");
        System.out.println();
        while (tmp < end) {
            System.out.println("Packet#: " + (tmp+1) + ""); // packet seq was from 0, add 1 to it to get it from 1
            System.out.println("Sender Log: " + ArrayToString(senderLog.get(tmp)));
            System.out.println("Receiver Log: " + ArrayToString(receiverLog.get(tmp)));
            System.out.println("Retransmission Times: " + countForReSend(senderLog.get(tmp)));
            System.out.println();
            tmp++;
        }
        System.out.println("----------------------------------------------------------------------------------------");
    }

    private static int countForReSend(ArrayList<String> arrayList) {
        int count = 0;
        for (String s : arrayList) {
            if (s == "Resent") {
                count++;
            }
        }
        return count;
    }

    private static String ArrayToString(ArrayList<String> arrayList) {
        String s = arrayList.toString();
        return s.replaceAll(",", " ->");
    }
}
