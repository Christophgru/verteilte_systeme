package uulm.in.vs.ex7.messages;


public class NetworkMessage implements Comparable<NetworkMessage> {
    private final int senderID;
    private final int delay;
    private final long timestamp;
    private final RaftMessage payload;

    public NetworkMessage(int senderID, int delay, RaftMessage payload) {
        this.senderID = senderID;
        this.delay = delay;
        this.timestamp = System.currentTimeMillis();
        this.payload = payload;
    }


    @Override
    public int compareTo(NetworkMessage other) {
        if(this.timestamp + this.delay < other.timestamp + other.delay)
            return -1;
        else if(this.timestamp + this.delay > other.timestamp + other.delay)
            return 1;
        return 0;
    }

    public long getDeliveryTime() {
        return timestamp + delay;
    }

    public int getSenderID() {
        return senderID;
    }

    public RaftMessage getPayload() {
        return payload;
    }
}
