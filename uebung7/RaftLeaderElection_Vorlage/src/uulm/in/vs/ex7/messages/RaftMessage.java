package uulm.in.vs.ex7.messages;

public abstract class RaftMessage {
    protected final int senderID;

    public RaftMessage(int senderID) {
        this.senderID = senderID;
    }

    public int getSenderID() {
        return senderID;
    }
}
