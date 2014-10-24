import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

/**
 * the rdt receiver class
 * <p/>
 * Name: Chenguang He
 * Email: readman@iastate.edu
 * Created by chenguanghe on 9/17/14.
 */
public class Receiver {
    private Random random = new Random(System.currentTimeMillis()); // to get the random number
    private int startWindow; // the begin pointer of windows
    private int Seq = 0; // the seq number
    public boolean isBlock;
    public HashMap<Integer, ArrayList<String>> log = new HashMap<Integer, ArrayList<String>>();//the queue have all packet with different state
    private DatagramSocket socket; // the socket
    private final int PACKET_SIZE = 512; // the size of packet
    private int windows[]; // the windwos
    private final int ACK = 1; // ack
    private final int NAK = 0; // nak
    private int maxWindowsSize; // the max size of windows
    private float lossRate, corruptRate; // the rate of loss and corrupt
    private int recPort;// the rec port
    private int sendPort; // the send port


    /**
     * no default constructor
     */
    private Receiver() {
    }

    /**
     * the useful constructor
     *
     * @param lossRate    the rate of loss
     * @param corruptRate the rate of corrupt packet
     * @param windowSize  windows size
     * @param recPort     the receive port
     * @param sendPort    the send port
     * @throws SocketException socket exception
     */
    public Receiver(float lossRate, float corruptRate, int windowSize, int recPort, int sendPort) throws SocketException {
        this.lossRate = lossRate;
        this.corruptRate = corruptRate;
        this.maxWindowsSize = windowSize;
        socket = new DatagramSocket(recPort);
        this.recPort = recPort;
        this.sendPort = sendPort;
        this.isBlock = false;
    }

    /**
     * write log
     *
     * @param seq // the seq number
     * @param s   the log
     */
    public void write(int seq, State s) {
        if (log.containsKey(seq)) {
            ArrayList<String> arrayList = log.get(seq);
            arrayList.add(s.toString());
        } else {
            ArrayList<String> arrayList = new ArrayList<String>();
            arrayList.add(s.toString());
            log.put(seq, arrayList);
        }
    }

    /**
     * receive packets
     *
     * @throws Exception socket exception
     */
    public void Receive() throws Exception {
        windows = new int[maxWindowsSize];
        Arrays.fill(windows, NAK);
        byte[] receivedData = new byte[PACKET_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);
        while (true) {
            isBlock = false;
            socket.receive(receivePacket); //keep getting packets
            isBlock = true;
            int CurrentSeq = getSeqNum(receivePacket);
            boolean isLoss = isLoss(CurrentSeq); // if it is loss
            if (isLoss)
                continue;
            write(CurrentSeq, State.Received);
            Corrupt(receivePacket);
            boolean isCorrupt = isCorrupted(receivePacket); // if it is corrupt
            if (isCorrupt)
                continue;
            else {
                ackPacket(CurrentSeq); // ack packet
                sendAcknowledgement(); // send acks
                adjustWindow(CurrentSeq);// adjust windows
            }
            isBlock = false;
        }
    }

    /**
     * corrupt a packet in a random rate
     *
     * @param pkt the packet
     */
    private void Corrupt(DatagramPacket pkt) {
        int rand = random.nextInt(10) + 1;
        if (rand <= corruptRate * 10) {
            String packetString= new String(pkt.getData());
            int index2 = packetString.indexOf("Data: ");
            pkt.getData()[index2+rand]++;
        }
    }

    /**
     * ack a packet
     *
     * @param seqNum the number of packet
     */
    private void ackPacket(int seqNum) {
        if (startWindow <= seqNum) {
            if (seqNum - startWindow < maxWindowsSize) {
                windows[seqNum - startWindow] = ACK;
            }
        }
        write(seqNum, State.Acked);
    }

    /**
     * adjust windows to the first nak position
     *
     * @param seqNum the number of packet
     */
    private void adjustWindow(int seqNum) {
        //shift window
        while (true) {
            if (windows[0] == ACK) {
                for (int i = 0; i < maxWindowsSize - 1; i++) {
                    windows[i] = windows[i + 1];
                }
                windows[maxWindowsSize - 1] = NAK;
                startWindow++;
            } else
                break;
        }
    }

    /**
     * set loss to a packet
     *
     * @param seq
     * @return
     */
    private boolean isLoss(int seq) {
        int rand = random.nextInt(10) + 1;
        if (rand <= lossRate * 10) {
            write(seq, State.Loss);
            return true;
        } else
            return false;
    }

    /**
     * get the seq number of packet
     *
     * @param pkt the packet
     * @return the seq number
     */
    private int getSeqNum(DatagramPacket pkt) {
        String packetString = new String(pkt.getData());
        int index = packetString.indexOf("Seq: ") + ("Seq: ".length());
        int index2 = packetString.indexOf("Data: ");
        int seq = -1;
        try {
            seq = Integer.parseInt(packetString.substring(index, index2));
        } catch (Exception e) {
        }
        return seq;
    }

    /**
     * test if string is number
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    /**
     * chekc if the packet is corrupted
     *
     * @param pkt the packet
     * @return true if it is corrected, otherwise false
     */
    private boolean isCorrupted(DatagramPacket pkt) {
        //get checksum from packet
        int seq = getSeqNum(pkt);
        String packetString = new String(pkt.getData());
        int index = packetString.indexOf("Checksum: ") + ("Checksum: ".length());
        if (index<0)
            return true;
        int index2 = packetString.indexOf("Seq:");
        if (index2<0)
            return true;
        String s = packetString.substring(index, index2).trim();
        if (!isNumeric(s))
            return true;
        int checksum = Integer.parseInt(s);
        String [] dataString = packetString.split("Data: ");
        if (dataString.length < 2){
            return true;
        }
        //compute checksum
        byte[] data = dataString[1].getBytes();

        int computedChecksum = 0;
        for (int i = 0; i < data.length; i++) {
            computedChecksum += (int) data[i];
        }

        //compare checksums
        if (computedChecksum == checksum)
            return false;
        else {
            write(seq, State.Corrupt);
            return true;
        }
    }

    /**
     * send ack to sender side
     *
     * @return the ack
     * @throws Exception
     */
    private String sendAcknowledgement() throws Exception {
        String ack = ("Seq: " + Seq + "  Window: ");
        for (int i = 0; i < maxWindowsSize; i++) {
            ack += windows[i];
        }
        ack += "\r\n";
        byte[] ackData = new byte[ack.length()];
        ackData = ack.getBytes();
        DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, InetAddress.getLocalHost(), sendPort);
        socket.send(ackPacket);
        return ack;
    }
}
