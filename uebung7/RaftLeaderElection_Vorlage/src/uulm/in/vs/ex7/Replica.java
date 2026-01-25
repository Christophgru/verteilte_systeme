package uulm.in.vs.ex7;

import uulm.in.vs.ex7.messages.*;
import uulm.in.vs.ex7.network.CommunicationHandler;
import uulm.in.vs.ex7.network.MessageQueue;
import uulm.in.vs.ex7.types.State;

import java.util.concurrent.*;
import java.util.concurrent.ThreadLocalRandom;

public class Replica {
    private final int replicaID;
    private final int numReplicas;

    // Raft persistent-ish state (simplified)
    private volatile int currentTerm = 0;
    private volatile int votedFor = -1;          // candidateId voted for in currentTerm (-1 = none)

    // Raft volatile state
    private volatile State currentState = State.FOLLOWER;
    private volatile int votesReceived = 0;

    private final MessageQueue messageInQueue;
    private final CommunicationHandler communicationHandler;

    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // Timers
    private volatile ScheduledFuture<?> electionTimeoutFuture;
    private volatile ScheduledFuture<?> heartbeatFuture;//triggers if we dont get a heartbeat

    // Timing (tune as needed)
    private static final long HEARTBEAT_INTERVAL_MS = 50;      // leader sends heartbeats every x ms
    private static final long ELECTION_TIMEOUT_MIN_MS = 200;   // follower/candidate election timeout range
    private static final long ELECTION_TIMEOUT_MAX_MS = 400;   // start new election if not elected or terminated after x ms

    public Replica(int replicaID, int numReplicas, CommunicationHandler communicationHandler) {
        this.replicaID = replicaID;
        this.numReplicas = numReplicas;

        this.messageInQueue = new MessageQueue();
        this.communicationHandler = communicationHandler;
        this.communicationHandler.registerChannel(replicaID, messageInQueue);

        this.executorService = Executors.newFixedThreadPool(1);
        this.executorService.submit(this::processMessages);

        resetElectionTimeout();
    }


    private long randomElectionTimeoutMs() {
        return ThreadLocalRandom.current().nextLong(ELECTION_TIMEOUT_MIN_MS, ELECTION_TIMEOUT_MAX_MS + 1);
    }

    private void resetElectionTimeout() {
        if (electionTimeoutFuture != null && !electionTimeoutFuture.isDone()) {
            electionTimeoutFuture.cancel(false);
        }
        electionTimeoutFuture = scheduler.schedule(
                this::onElectionTimeout,
                randomElectionTimeoutMs(),
                TimeUnit.MILLISECONDS
        );
    }

    private void cancelElectionTimeout() {
        if (electionTimeoutFuture != null && !electionTimeoutFuture.isDone()) {
            electionTimeoutFuture.cancel(false);
        }
    }

    private void startHeartbeats() {
        stopHeartbeats();
        heartbeatFuture = scheduler.scheduleAtFixedRate(
                this::sendHeartbeat,
                0,
                HEARTBEAT_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
    }

    private void stopHeartbeats() {
        if (heartbeatFuture != null && !heartbeatFuture.isDone()) {
            heartbeatFuture.cancel(false);
        }
    }


    private void sendHeartbeat() {
        if (currentState != State.LEADER) return;
        communicationHandler.broadcast(new HeartbeatMessage(replicaID, currentTerm));
    }

    private void onElectionTimeout() {
        //followers and candidates start a new election when election timeout fires.
        if (currentState == State.LEADER) return;
        startElection();
    }

    private void startElection() {
        // Become candidate, increment term, vote for self
        currentState = State.CANDIDATE;
        currentTerm++;
        votedFor = replicaID;
        votesReceived = 1; // self vote

        System.out.println("Replica " + replicaID + " starts election for term " + currentTerm);

        // Send vote requests
        communicationHandler.broadcast(new VoteRequestMessage(replicaID, currentTerm));

        // Reset election timeout so we can try again if split vote
        resetElectionTimeout();
    }

    private void becomeFollower(int newTerm) {
        // Step down and adopt term
        currentState = State.FOLLOWER;
        currentTerm = newTerm;
        votedFor = -1;
        votesReceived = 0;

        stopHeartbeats();
        resetElectionTimeout();
    }

    private void becomeLeader() {
        currentState = State.LEADER;
        cancelElectionTimeout();  // leader doesn’t use election timeout

        System.out.println("Replica " + replicaID + " becomes LEADER for term " + currentTerm);

        startHeartbeats();
    }

    private void processMessages() {
        while (true) {
            try {
                NetworkMessage message = messageInQueue.take();
                if (message.getPayload() instanceof HeartbeatMessage hb) {
                    handleHeartbeat(hb);
                } else if (message.getPayload() instanceof VoteRequestMessage vr) {
                    handleVoteRequest(vr);
                } else if (message.getPayload() instanceof VoteResponseMessage vresp) {
                    handleVoteResponse(vresp);
                }

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleHeartbeat(HeartbeatMessage hb) {
        int term = hb.getCurrentTerm();

        // If heartbeat term is newer -> step down
        if (term > currentTerm) {
            becomeFollower(term);
            // after becomeFollower() election timeout is reset
            return;
        }

        // If heartbeat term is older -> ignore
        if (term < currentTerm) {
            return;
        }

        // term == currentTerm: valid leader heartbeat
        if (currentState != State.FOLLOWER) {
            // Candidate or leader seeing a leader in same term -> become follower
            currentState = State.FOLLOWER;
            stopHeartbeats();
        }

        // Reset election timeout on valid heartbeat
        resetElectionTimeout();
    }

    private void handleVoteRequest(VoteRequestMessage req) {
        int term = req.getCurrentTerm();
        int candidateId = req.getSenderID();

        // If request term is newer, step down and update term first
        if (term > currentTerm) {
            becomeFollower(term);
        }

        // If request term is older, reject
        if (term < currentTerm) {
            communicationHandler.send(candidateId, new VoteResponseMessage(replicaID, currentTerm, false));
            return;
        }

        // term == currentTerm: grant vote if haven't voted or already voted for same candidate
        boolean canVote = (votedFor == -1 || votedFor == candidateId);

        if (canVote) {
            votedFor = candidateId;

            //reset election timeout when granting vote
            resetElectionTimeout();

            communicationHandler.send(candidateId, new VoteResponseMessage(replicaID, currentTerm, true));
        } else {
            communicationHandler.send(candidateId, new VoteResponseMessage(replicaID, currentTerm, false));
        }
    }

    private void handleVoteResponse(VoteResponseMessage resp) {
        int term = resp.getCurrentTerm();

        // If response has newer term -> step down
        if (term > currentTerm) {
            becomeFollower(term);
            return;
        }

        // Ignore responses for old terms or if not a candidate
        if (term < currentTerm || currentState != State.CANDIDATE) {
            return;
        }

        if (resp.isVoteGranted()) {
            votesReceived++;
            System.out.println("Replica " + replicaID + " got " + votesReceived + " votes in term " + currentTerm);

            if (votesReceived >= (numReplicas / 2 + 1)) {
                becomeLeader();
            }
        }
    }


    public void shutdown() {
        scheduler.shutdownNow();
        executorService.shutdownNow();
    }

    public State getCurrentState() {
        return currentState;
    }
}
