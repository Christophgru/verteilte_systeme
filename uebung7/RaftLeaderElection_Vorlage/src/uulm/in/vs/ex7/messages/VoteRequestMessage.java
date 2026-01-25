package uulm.in.vs.ex7.messages;

public class VoteRequestMessage extends RaftMessage {
    private final int currentTerm;

    public VoteRequestMessage(int senderID, int currentTerm) {
        super(senderID);

        this.currentTerm = currentTerm;
    }

    public int getCurrentTerm() {
        return currentTerm;
    }
}
