package uulm.in.vs.ex7.messages;

public class VoteResponseMessage extends RaftMessage {
    private final int currentTerm;
    private final boolean voteGranted;

    public VoteResponseMessage(int senderID, int currentTerm, boolean voteGranted) {
        super(senderID);

        this.currentTerm = currentTerm;
        this.voteGranted = voteGranted;
    }

    public int getCurrentTerm() {
        return currentTerm;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }
}
