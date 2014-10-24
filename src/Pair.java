/**
 * the pair of sender and receiver
 * Name: Chenguang He
 * Email: readman@iastate.edu
 * Created by chenguanghe on 9/28/14.
 */
public class Pair {
    Sender sender;
    Receiver receiver;
    int sendPort;
    int recPort;
    boolean isBlocked;

    public Pair(Sender sender, Receiver receiver, int sendPort, int recPort) {
        this.sender = sender;
        this.recPort = recPort;
        this.receiver = receiver;
        this.sendPort = sendPort;
        isBlocked = sender.isBlock() && receiver.isBlock;
    }

    public boolean isBlocked() throws InterruptedException {
        return sender.isBlock || receiver.isBlock;
    }

    /**
     * if send or receive port is same, then it is equal, because we need to distinguish different pair by Hashmap
     *
     * @param obj
     * @return
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof Pair))
            return false;
        if (obj == this)
            return true;

        Pair rhs = (Pair) obj;
        return rhs.recPort == this.recPort || rhs.sendPort == this.sendPort;
    }
}
