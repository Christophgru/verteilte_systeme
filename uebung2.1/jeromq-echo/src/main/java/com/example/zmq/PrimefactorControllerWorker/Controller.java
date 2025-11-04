package com.example.zmq.PrimefactorControllerWorker;

import java.math.BigInteger;

import org.zeromq.ZContext;

import com.example.zmq.net.JeromqPushSocket;
import com.example.zmq.net.JeromqPullSocket;
//import big integer

public class Controller {

    private final JeromqPushSocket localPushBufferSocketIn;
    private final JeromqPullSocket localBufferSocketOut;
    private int counter = 0;

    public Controller(ZContext ctx, String workerinterfaceEndpointIn, String workerinterfaceEndpointOut) {
        this.localPushBufferSocketIn = new JeromqPushSocket(ctx, workerinterfaceEndpointIn, true);
        this.localBufferSocketOut = new JeromqPullSocket(ctx, workerinterfaceEndpointOut, true);
        localBufferSocketOut.setReceiveTimeOut(50); // milliseconds

    }

    public void addJob(BigInteger numberToFactor) {
        counter++;
        // Start the controller logic here
        System.out.println("Adding job to factor number: " + numberToFactor);
        localPushBufferSocketIn.push(String.valueOf(numberToFactor));
    }

    public String getResult() {
        // Start the controller logic here
        String message = localBufferSocketOut.recv();
        if (message != null) {
            counter--;
            System.out.println("Received result: " + message);
            return message;
        }
        return null;
    }

    public boolean allJobsCollected() {
        System.out.println("waiting for jobs to be collected, remaining: " + counter);
        return (counter == 0);
    }

    public void stop() {
        // Stop the controller logic here
        localPushBufferSocketIn.close();
        localBufferSocketOut.close();
    }

}
