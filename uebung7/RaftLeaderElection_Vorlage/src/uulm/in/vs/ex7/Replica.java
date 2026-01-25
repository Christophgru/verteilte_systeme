package uulm.in.vs.ex7;

import uulm.in.vs.ex7.messages.*;
import uulm.in.vs.ex7.network.CommunicationHandler;
import uulm.in.vs.ex7.network.MessageQueue;
import uulm.in.vs.ex7.types.State;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

public class Replica {
    private final int replicaID;
    private final int numReplicas;
    private int currentTerm=0;

    private State currentState;
    private final MessageQueue messageInQueue;
    private final CommunicationHandler communicationHandler;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private volatile ScheduledFuture<?> heartbeatTimeoutFuture;
    private volatile ScheduledFuture<?> sendTimeoutFuture;
    private final long   heartbeat_timeout_delay=95;
    private final long vote_delay=300;
    private final long heartbeat_freq=90;
    private volatile ScheduledFuture<?> voteTimeoutFuture;
    private volatile int voted_for_me=0;
    private volatile boolean votedThisTermAlready=false;
    private volatile int active_leader=-1;


    public Replica(int replicaID, int numReplicas, CommunicationHandler communicationHandler) {
        this.replicaID = replicaID;
        this.numReplicas = numReplicas;

        this.currentState = State.FOLLOWER;
        this.messageInQueue = new MessageQueue();
        this.communicationHandler = communicationHandler;
        this.communicationHandler.registerChannel(replicaID, messageInQueue);

        this.executorService = Executors.newFixedThreadPool(1);
        this.executorService.submit(this::processMessages);
        resetHeartbeatTimeout();
    }
    private void sentHeartBeat(){
        HeartbeatMessage hbm=new HeartbeatMessage(replicaID,currentTerm);
        communicationHandler.broadcast(hbm);
    }
    private void resetHeartbeatTimeout() {
        if (heartbeatTimeoutFuture != null && !heartbeatTimeoutFuture.isDone()) {
            heartbeatTimeoutFuture.cancel(false);
        }
        heartbeatTimeoutFuture = scheduler.schedule(
                this::heartbeatTimeout,
                heartbeat_timeout_delay,
                TimeUnit.MILLISECONDS
        );
    }
        private void resetVoteTimeout() {
            if (voteTimeoutFuture != null && !voteTimeoutFuture.isDone()) {
                voteTimeoutFuture.cancel(false);
            }
        voteTimeoutFuture= scheduler.schedule(
                this::voteTimeout,
                vote_delay,
                TimeUnit.MILLISECONDS);
    }
    private void heartbeatTimeout(){
        System.out.println("Heartbeat timeout triggered in process "+this.replicaID);
        //init vote
        if(currentState==State.FOLLOWER){
            leaderElectionProcess();
        }
    }
    private void voteTimeout(){
        System.out.println("Vote timeout triggered in process "+this.replicaID);
        //stop vote
        leaderElectionProcess();
    }
    private void leaderElectionProcess(){
        System.out.println("Started leader election, Candidate: "+replicaID);
        voted_for_me=0;
        votedThisTermAlready=false;
        this.currentState=State.CANDIDATE;
        this.currentTerm++;
        VoteRequestMessage voteRequestMessage=new VoteRequestMessage(replicaID,currentTerm);
        communicationHandler.broadcast(voteRequestMessage);
        resetVoteTimeout();
    }

    private void processMessages() {
        while (true) {
            try {
                NetworkMessage message = messageInQueue.take();
                if (message.getPayload() instanceof HeartbeatMessage heartbeat) {
                    //reset timeout
                    if(heartbeat.getCurrentTerm()>=currentTerm){
                        if(currentState==State.LEADER){
                            sendTimeoutFuture.cancel(false);
                        }
                        currentState=State.FOLLOWER;
                        currentTerm=heartbeat.getCurrentTerm();
                        resetHeartbeatTimeout();
                    }
                    if(currentState==State.FOLLOWER){
                        resetHeartbeatTimeout();
                    }
                }else if(message.getPayload() instanceof VoteRequestMessage voteRequest){
                    VoteResponseMessage voteResponse;
                    if(voteRequest.getCurrentTerm()>=currentTerm&&!votedThisTermAlready){
                        votedThisTermAlready=true;
                        currentTerm=voteRequest.getCurrentTerm();
                        voteResponse=new VoteResponseMessage(replicaID,currentTerm,true);
                         active_leader=voteRequest.getSenderID();
                    }else {
                         voteResponse = new VoteResponseMessage(replicaID, currentTerm, false);
                    }
                    resetHeartbeatTimeout();
                     communicationHandler.send(voteRequest.getSenderID(),voteResponse);
                }else if(message.getPayload() instanceof VoteResponseMessage voteResponse){
                    if(voteResponse.isVoteGranted()){
                        voted_for_me++;
                        System.out.println("Process "+replicaID+" got "+voted_for_me+" Votes");

                    }
                    if(voteResponse.getCurrentTerm()>=currentTerm){
                        //transisition to Follower mode
                        System.out.println("Process "+replicaID+" stopped vote process due to newer Term!");
                        currentState=State.FOLLOWER;
                        currentTerm=voteResponse.getCurrentTerm();
                        resetHeartbeatTimeout();
                    }
                    if(voted_for_me>=numReplicas/2+1){
                        System.out.println("Process "+replicaID+" is the new Leader!");
                        sendTimeoutFuture =scheduler.scheduleAtFixedRate(this::sentHeartBeat, (long)((float)vote_delay*(1.0+(float)Math.random())),heartbeat_freq,TimeUnit.MILLISECONDS);

                        currentState=State.LEADER;
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void shutdown() {
        scheduler.shutdown();
        this.executorService.shutdownNow();
    }

    public State getCurrentState() {
        return this.currentState;
    }

}
