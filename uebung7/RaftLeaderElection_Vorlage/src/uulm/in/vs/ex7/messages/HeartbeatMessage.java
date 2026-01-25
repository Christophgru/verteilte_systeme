package uulm.in.vs.ex7.messages;

public class HeartbeatMessage extends RaftMessage {
    private final int currentTerm;

    public HeartbeatMessage(int senderID, int currentTerm) {
        super(senderID);

        this.currentTerm = currentTerm;
    }

    public int getCurrentTerm() {
        return currentTerm;
    }
}
