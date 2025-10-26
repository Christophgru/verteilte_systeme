package com.example.zmq.PrimefactorControllerWorker;
import org.zeromq.ZContext;
import com.example.zmq.net.JeromqPullSocket;
import com.example.zmq.net.JeromqPushSocket;
import com.example.zmq.PrimefactorControllerWorker.Fermat;
import java.math.BigInteger;

public class Worker {

    private final JeromqPullSocket pullSocket;
    private final JeromqPushSocket pushSocket;
    boolean running=true;

    public Worker(ZContext ctx, String connectionWorkerIn, String workerinterfaceEndpointOut) {
        this.pullSocket = new JeromqPullSocket(ctx, connectionWorkerIn,false);
        pullSocket.setReceiveTimeOut(500); // milliseconds
        this.pushSocket = new JeromqPushSocket(ctx, workerinterfaceEndpointOut,false);
    }

    public void start() {
        System.out.println("Worker started");
        // Start the worker logic here
        while(running){
            String message = pullSocket.recv();
            if (message == null) continue; // timeout occurred, loop again
            System.out.println("Received message to factor: " + message);
            // Simple prime factorization logic
            BigInteger number = new BigInteger(message);
            BigInteger[] resultList = Fermat.fermatFactorization(number);
            System.out.println("Sending factors: " + resultList[0] + ", " + resultList[1]);
            //put numbers intos string such that "n:p:q" where n is the number to factor and p and q are the factors
            String result="%d:%d:%d".formatted(number,resultList[0],resultList[1]);
            pushSocket.push(result);
        }
    }

    public void stop() {
        // Stop the worker logic here
        running=false;
        pushSocket.close();
        //sleep for 500ms to ensure that the worker loop can exit if it is waiting for a message
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        pullSocket.close();
    }

}
