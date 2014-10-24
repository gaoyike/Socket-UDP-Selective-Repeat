/**
 * The packet class to build packet for datagram.
 * Name: Chenguang He
 * Email: readman@iastate.edu
 */
public class Packet {
    private int seq; // the seq number
    private boolean acked; // if it is acked
    private byte[] payload; // the datagram
    private int len; // the length of data
    public State state; // the state of packet
    public int reTransmits = 0; // the times of retransmits

    /**
     * no default constructor
     */
    private Packet() {
    }

    public Packet(Packet p) {
        this.acked = p.acked;
        this.len = p.len;
        this.payload = p.payload;
        this.state = p.state;
        this.reTransmits = p.reTransmits;
    }

    /**
     * the useful constructor
     *
     * @param seq     the number of seq
     * @param payload the datagram
     * @param state   the state of packet
     */
    public Packet(int seq, byte[] payload, State state) {
        this.seq = seq;
        this.payload = payload;
        this.state = state;
        this.len = payload.length;
    }

    /**
     * set seq number
     *
     * @param seq the number of seq
     */
    public void setSeq(int seq) {
        this.seq = seq;
    }

    /**
     * set ack to packet
     *
     * @param ack the ack
     */
    public void setAck(boolean ack) {
        this.acked = ack;
    }

    /**
     * set payload
     *
     * @param payload the datagram
     */
    public void setPayLoad(byte[] payload) {
        this.payload = payload;
    }

    /**
     * return the payload
     *
     * @return the payload
     */
    public byte[] getPayload() {
        return payload;

    }

    /**
     * get seq number
     *
     * @return the number of seq
     */
    public int getSeq() {
        return seq;
    }

    /**
     * check if it is acked
     *
     * @return true if acked otherwise false
     */
    public boolean isAcked() {
        return acked;
    }

    /**
     * the packet class override tostring
     *
     * @return return string
     */
    public String toString() {
        return ("Seq: " + seq + " 's state is " + state.toString() + " , it is " + reTransmits + " times to retransmits");
    }

}
