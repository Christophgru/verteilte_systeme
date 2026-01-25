package uulm.in.vs.ex7;

import uulm.in.vs.ex7.network.CommunicationHandler;
import uulm.in.vs.ex7.types.State;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        int numReplicas = 7;
        Replica[] replicas = new Replica[numReplicas];

        CommunicationHandler communicationHandler = new CommunicationHandler(numReplicas, 30);

        System.out.println("Starting replicas");
        for(int i = 0; i < replicas.length; i++) {
            replicas[i] = new Replica(i, numReplicas, communicationHandler);
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for(int i = 0; i < replicas.length; i++) {
            if(replicas[i].getCurrentState().equals(State.LEADER)) {
                System.out.println("Shutting down replica " + i);
                replicas[i].shutdown();
            }
        }

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Shutting down all replicas");
        for(int i = 0; i < replicas.length; i++) {
            replicas[i].shutdown();
        }
    }
}
