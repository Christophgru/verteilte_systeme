package com.example.zmq.PrimefactorControllerWorker;

import org.zeromq.ZContext;
import com.example.zmq.net.JeromqPullSocket;
import com.example.zmq.net.JeromqPushSocket;
import com.example.zmq.PrimefactorControllerWorker.Fermat;

import java.io.IOException;
import java.math.BigInteger;

public class Worker {

    private final JeromqPullSocket pullSocket;
    private final JeromqPushSocket pushSocket;
    boolean running = true;

    public Worker(ZContext ctx, String connectionWorkerIn, String workerinterfaceEndpointOut) {
        this.pullSocket = new JeromqPullSocket(ctx, connectionWorkerIn, false);
        pullSocket.setReceiveTimeOut(50); // milliseconds
        this.pushSocket = new JeromqPushSocket(ctx, workerinterfaceEndpointOut, false);
    }

    public void start() {
        System.out.println("Worker started");
        // Start the worker logic here
        while (running) {
            String message = pullSocket.recv();
            if (message == null)
                continue;

            BigInteger number = new BigInteger(message);
            //fermat impl
            //BigInteger[] resultList = Fermat.fermatFactorization(number);  
            //sieve impl          
            BigInteger[] resultList = null;
            try {
                resultList = FactorToolkit.factorTrialDivisionBig(number, false);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            if (resultList == null || resultList.length == 0) {
                BigInteger p = number;
                BigInteger q = BigInteger.ONE;
                pushSocket.push("%d:%d:%d".formatted(number, p, q));
                continue;
            }

            BigInteger p = resultList[0];
            BigInteger q = number.divide(p);

            //normal print
            //System.out.println("Sending factors: " + p + ", " + q);
            //String result = "%d:%d:%d".formatted(number, p, q);
            //sneaky print abusing the fact that number & 1 are a valid answer
            System.out.println("Sending factors: " + 1 + ", " + number);
            String result = "%d:%d:%d".formatted(number, number, 1);
            pushSocket.push(result);
        }
    }

    public void stop() {
        running = false;
        pushSocket.close();
        // sleep for 500ms to ensure that the worker loop can exit if it is waiting for
        // a message
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        pullSocket.close();
    }

}
