package uulm.in.vs.ex7.network;

import uulm.in.vs.ex7.messages.NetworkMessage;
import uulm.in.vs.ex7.messages.RaftMessage;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class CommunicationHandler {
    private final HashMap<Integer, MessageQueue> messageQueues;
    private final ReentrantLock sendLock;
    private final int numReplicas;

    private final int[][] latencyMatrix;

    public CommunicationHandler(int numReplicas, int maxNetworkDelay) {
        this.numReplicas = numReplicas;
        this.sendLock = new ReentrantLock(true);
        this.messageQueues = new HashMap<>();
        this.latencyMatrix = generateRandomLatencyMatrix(maxNetworkDelay);
    }

    public CommunicationHandler(int numReplicas, int[][] latencyMatrix) {
        this.numReplicas = numReplicas;
        this.sendLock = new ReentrantLock(true);
        this.messageQueues = new HashMap<>();

        if(latencyMatrix.length == numReplicas) {
            for(int i = 0; i < numReplicas; i++) {
                if(latencyMatrix[i].length != numReplicas) {
                    throw new IllegalArgumentException("Latency matrix does not have the right format");
                }
            }
        } else {
            throw new IllegalArgumentException("Latency matrix does not have the right format");
        }

        this.latencyMatrix = latencyMatrix;
    }

    public void registerChannel(int replicaID, MessageQueue queue) {
        try {
            this.sendLock.lock();
            System.out.println("Replica " + replicaID + " registered channel");
            this.messageQueues.put(replicaID, queue);
        } finally {
            this.sendLock.unlock();
        }
    }

    public void broadcast(RaftMessage msg) {
        try {
            sendLock.lock();
            for (int receiverID : this.messageQueues.keySet()) {
                //System.out.println("Sending broadcast to replica " + receiverID);
                messageQueues.get(receiverID).add(new NetworkMessage(msg.getSenderID(),
                        latencyMatrix[msg.getSenderID()][receiverID], msg));
            }
        } finally {
            sendLock.unlock();
        }
    }

    public void send(int receiverID, RaftMessage msg) {
        try {
            sendLock.lock();
            messageQueues.get(receiverID).add(new NetworkMessage(msg.getSenderID(),
                    latencyMatrix[msg.getSenderID()][receiverID], msg));
        } finally {
            sendLock.unlock();
        }
    }

    private int[][] generateRandomLatencyMatrix(int maxDelay) {
        Random rand = new Random();
        int[][] latencyMatrix = new int[numReplicas][numReplicas];
        for (int y = 0; y < numReplicas; y++) {
            for (int x = 0; x < numReplicas; x++) {
                if (x == y)
                    latencyMatrix[x][y] = 0;
                else if (x > y)
                    latencyMatrix[x][y] = rand.nextInt(maxDelay);
                else
                    latencyMatrix[x][y] = latencyMatrix[y][x];
            }
        }
        return latencyMatrix;
    }
}
