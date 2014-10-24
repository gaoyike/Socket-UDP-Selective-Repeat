import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * The rdt sender class.
 *
 * Name: Chenguang He
 * Email: readman@iastate.edu
 * Created by chenguanghe on 9/17/14.
 */
public class Sender{
    private int windowSize;  // real windows size
    private int timeOut; // the time of timeout
    private int Seq = 0; // the seq number of packet
    public boolean isBlock; // if the current packet is sending
    private ArrayBlockingQueue<Packet> queue = new ArrayBlockingQueue(99999); // the large queue for message
    private LinkedList<Packet> WindowsList; // the window
    public HashMap<Integer, ArrayList<String>> log = new HashMap<Integer, ArrayList<String>>();//the queue have all packet with different state
    private DatagramSocket socket; // the socket
    private Timer timeoutTimer; // the timer to schedule timeout
    private final int PACKET_SIZE = 512;//the size of packet
    private int windows[]; // the windows to get the feedback from client
    private final int ACK = 1; // ack
    private final int NAK = 0; // nak
    private int numberOfTimeouts; // the number of timeouts
    private int maxWindowsSize; // the limit of windows size
    private int recPort; // the receive port
    private int sendPort; // the send port

    /**
     * no default public constructor
     */
    private Sender() {
    }

    /**
     * the public constructor to build the sender
     * @param windowsSize the limit of windows
     * @param timeout the time of timeout
     * @param recPort the receive port
     * @param sendPort the send port
     * @throws SocketException // socket exception
     */
    public Sender(int windowsSize, int timeout,int recPort, int sendPort) throws SocketException {
        this.maxWindowsSize = windowsSize;
        this.timeOut = timeout;
        windows = new int[maxWindowsSize];
        socket = new DatagramSocket(recPort);
        WindowsList = new LinkedList<Packet>();
        this.recPort = recPort;
        this.sendPort = sendPort;
    }

    /**
     * put data to queue
     *
     * @param buf the data
     * @param len the length of data
     */
    public void pushToQueue(char[] buf, int len) {
        try {
            Packet p = new Packet(Seq++, CharToByte(buf), State.Ready);
            queue.put(p);
            write(p.getSeq(), State.Ready);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * write log
     * @param seq // the seq number
     * @param s the log
     */
    public void write(int seq, State s) {
        if (log.containsKey(seq)){
           ArrayList<String> arrayList = log.get(seq);
            arrayList.add(s.toString());
        }else {
            ArrayList<String> arrayList = new   ArrayList<String>();
            arrayList.add(s.toString());
            log.put(seq, arrayList);
        }
    }

    /**
     * Send method use selective repeat
     */
    public void SendData() throws Exception {
        isBlock = true; // in transmission, block all traffic
        numberOfTimeouts = 0; // times of timeouts
        byte[] packetData = new byte[PACKET_SIZE]; // the packet data
        timeoutTimer = new Timer(true); // sent timer
        windowSize = 0; //size of windows
        while (true) {
            while (queue.isEmpty()&&windowSize == 0) {
                isBlock = false;
            }
            if (windowSize == 0) { // if it is the first time to send
                isBlock = true;
                windowSize = Math.min(queue.size(), maxWindowsSize);
                windows = new int[windowSize];
                Arrays.fill(windows, NAK);
                for (int i = 0; i < windowSize; i++) {
                    Packet p = queue.take();
                    p.state = State.Sent;
                    WindowsList.addLast(p);
                    write(p.getSeq(), State.Sent);
                    SendPacket(p);
                }
            } else {
                isBlock = true;
                int emptySpace = adjustWindow();
                int[] newWindows = new int[windowSize];
                int ping = 0; // the variable to set windows
                //adjust list of sending windows
                for (int i = 0; i < emptySpace; i++) {
                    WindowsList.removeFirst();
                }
                // merge to new windows
                for (int i = emptySpace; i < windowSize; i++) {
                    newWindows[ping] = windows[i];
                    ping++;
                }
                // send new packet
                while (emptySpace-- != 0 && !queue.isEmpty()) {
                    Packet p = queue.poll();
                    p.state = State.Sent;
                    WindowsList.addLast(p);
                    SendPacket(p);
                    write(p.getSeq(), State.Sent);
                }
                // merge windows
                windows = newWindows;
                windowSize = WindowsList.size();
            }
            if (windowSize != 0) {
                isBlock = true;
                byte[] ackData = new byte[PACKET_SIZE];
                DatagramPacket getAck = new DatagramPacket(ackData, ackData.length);
                socket.receive(getAck);
                ack(getAck);
            } else {
                isBlock = false;
                windowSize = Math.min(queue.size(), maxWindowsSize);
            }
        }
    }

    /**
     * ack packet in datagram packet
     * @param packet the packet
     */
    private void ack(DatagramPacket packet) {
        int seq = getSeq(packet);
        String packetString = new String(packet.getData());
        int index = packetString.indexOf("Window: ") + ("Window: ".length()); // setup a protocol for transmits.
        for (int i = 0; i < windowSize; i++) {
            int ack = Integer.parseInt(packetString.substring(index, index + 1).trim());
            if (ack == ACK) { // if it acked
                windows[i] = ACK;
                Packet p = WindowsList.get(i);
                p.setAck(true);
                p.state = State.Acked;
                write(p.getSeq(), State.Acked);
            }
            index++;
        }
    }

    /**
     * parse the string from client and return seq nubmer
     * @param pkt  the datagrampacket
     * @return the seq number
     */
    private static int getSeq(DatagramPacket pkt) {
        String packetString = new String(pkt.getData());
        int index = packetString.indexOf("Seq: ") + ("Seq: ".length());
        int seq = Integer.parseInt(packetString.substring(index, index + 3).trim());
        return seq;
    }

    /**
     * send a packet to client
     * @param packet the packet
     * @throws Exception the socket exception
     */
    private void SendPacket(Packet packet) throws Exception {
        byte[] message = packet.getPayload();
        byte[] packetData = new byte[PACKET_SIZE];
        createPacket(packetData, message, packet.getSeq());
        DatagramPacket pkt = new DatagramPacket(packetData, packetData.length, InetAddress.getLocalHost(), sendPort);
        socket.send(pkt);
        timeoutTimer.schedule(new PacketTimeout(packet), timeOut); // when send a packet, set a timer as well
    }

    /**
     * create a packet
     * @param packetData the full data in array of byte
     * @param message // the message data
     * @param seq // the seq number
     */
    private void createPacket(byte[] packetData, byte[] message, int seq) {
        Arrays.fill(packetData, (byte) 0);
        String header = new String("Checksum: " + CheckSum(message)+"Seq: " + seq + "Data: ");
        byte[] headerData = header.getBytes();
        for (int i = 0; i < headerData.length; i++) {
            packetData[i] = headerData[i];
        }
        for (int i = 0; i < message.length; i++) {
            packetData[headerData.length + i] = message[i];
        }
    }

    /**
     * transform arrays of char to array of byte
     * @param chars the char array
     * @return byte array
     */
    private byte[] CharToByte(char[] chars) {
        return new String(chars).getBytes();
    }

    /**
     * return if it is blocked
     * @return true it is in block, otherwise false;
     */
    public boolean isBlock() {
        return isBlock;
    }

    /**
     * the checksum for datagram
     * @param message the data
     * @return the checksum
     */
    private static int CheckSum(byte[] message) {
        int checksum = 0;
        for (int i = 0; i < message.length; i++) {
            checksum += message[i];
        }
        return checksum;
    }
    /**
     * the send method which in a new thread to put data into queue
     */
    private class Send extends Thread {
        Sender sender;

        public Send(Sender sender) {
            this.sender = sender;
        }

        public void run() {
            while (true) {
                try {
                    sender.SendData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * the method will move the first nak in windows to the first position.
     * @return the number of shifts
     * @throws Exception the exception
     */
    private int adjustWindow() throws Exception {
        int windowMoved = 0;
        for (int i = 0; i < windowSize; i++) {
            if (windows[i] == ACK)
                windowMoved++;
            else
                break;
        }
        return windowMoved;
    }

    /**
     * the timer for packet use to set up the timeout.
     */
    private class PacketTimeout extends TimerTask {
        private Packet p;

        public PacketTimeout(Packet p) {
            this.p = p;
        }

        public void run() {
            //if packet has not been ACKed
            numberOfTimeouts++;
            try {
                if (!p.isAcked()) {
                    SendPacket(p);
                    p.reTransmits++;
                    p.state = State.Resent;
                    write(p.getSeq(), State.Resent);
                }
            } catch (Exception e) {
            }
        }
    }
}
