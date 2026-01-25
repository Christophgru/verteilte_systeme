package uulm.in.vs.ex7;

import uulm.in.vs.ex7.messages.HeartbeatMessage;
import uulm.in.vs.ex7.messages.NetworkMessage;
import uulm.in.vs.ex7.messages.VoteRequestMessage;
import uulm.in.vs.ex7.messages.VoteResponseMessage;
import uulm.in.vs.ex7.network.CommunicationHandler;
import uulm.in.vs.ex7.network.MessageQueue;
import uulm.in.vs.ex7.types.State;

import java.util.Random;
import java.util.concurrent.*;

public class Replica {
    private final int replicaID;
    private final int numReplicas;

    private State currentState;
    private final MessageQueue messageInQueue;
    private final CommunicationHandler communicationHandler;
    private final ExecutorService executorService;


    public Replica(int replicaID, int numReplicas, CommunicationHandler communicationHandler) {
        this.replicaID = replicaID;
        this.numReplicas = numReplicas;

        this.currentState = State.FOLLOWER;
        this.messageInQueue = new MessageQueue();
        this.communicationHandler = communicationHandler;
        this.communicationHandler.registerChannel(replicaID, messageInQueue);

        this.executorService = Executors.newFixedThreadPool(1);
        this.executorService.submit(this::processMessages);
        //todo add timeout

    }

    private void processMessages() {
        while (true) {
            try {
                NetworkMessage message = messageInQueue.take();
                if (message.getPayload() instanceof HeartbeatMessage heartbeat) {
                    // TODO
                }else if(true){}
                // TODO
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void shutdown() {
        this.executorService.shutdownNow();
    }

    public State getCurrentState() {
        return this.currentState;
    }

}
