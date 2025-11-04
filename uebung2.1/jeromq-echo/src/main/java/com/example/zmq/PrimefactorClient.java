package com.example.zmq;

import org.zeromq.ZContext;
import com.example.zmq.PrimefactorControllerWorker.Controller;
import com.example.zmq.PrimefactorControllerWorker.Worker;
import com.example.zmq.net.JeromqReqSocket;
import com.example.zmq.net.JeromqSubscriberSocket;
import java.math.BigInteger;

//compile from jeromq-echo using 
//mvn -q -DskipTests package
//run: java -cp target/jeromq-echo-1.0-SNAPSHOT-shaded.jar com.example.zmq.PrimefactorClient or java -cp target/jeromq-echo-1.0-SNAPSHOT.jar com.example.zmq.PrimefactorClient
public class PrimefactorClient {

    public static void main(String[] args) {
        int numberOfMessages = 50;
        int numberOfWorkers = 10;
        ZContext ctx = new ZContext();
        // jeromq publisher service endpoint that provides number to factor
        String endpoint = "tcp://vs.lxd-vs.uni-ulm.de:27378";
        JeromqSubscriberSocket subscriberSocket = new JeromqSubscriberSocket(ctx, endpoint);
        // request 10 messages
        // get starting time
        long startTime = System.currentTimeMillis();

        // setup controller and worker
        String connectionWorkerIn = "tcp://*:27379";
        // start 10 workers on different ports
        String connectionWorkerOut = "tcp://*:27380";
        // spawn workers
        Worker[] workers = new Worker[numberOfWorkers];

        for (int i = 0; i < numberOfWorkers; i++) {
            workers[i] = new Worker(ctx, connectionWorkerIn, connectionWorkerOut);
            new Thread(workers[i]::start).start();
        }
        Controller controller = new Controller(ctx, connectionWorkerIn, connectionWorkerOut);
        for (int i = 0; i < numberOfMessages; i++) {
            String msg = subscriberSocket.receiveMessage();
            System.out.println("Received message " + (i + 1) + ": " + msg);
            BigInteger numberToFactor = new BigInteger(msg);
            controller.addJob(numberToFactor);

        }
        subscriberSocket.close();

        // verify result
        String verificationEndpoint = "tcp://vs.lxd-vs.uni-ulm.de:27379";
        JeromqReqSocket verificationSocket = new JeromqReqSocket(ctx, verificationEndpoint, 2000);
        // receive verification reply
        int counter_success = 0;
        int counter_total = 0;
        while (controller.allJobsCollected() == false) {
            String result = controller.getResult();
            if (result != null) {
                System.out.println("Sending result for verification: " + result);

                String verificationReply = verificationSocket.request(result);
                System.out.println("Verification reply: " + verificationReply);
                System.out.println("Result: " + result + " verified as " + verificationReply);
                if (verificationReply.contains("correct")) {
                    counter_success++;
                }
                counter_total++;
                System.out.println("Current success rate: " + counter_success + "/" + counter_total);

            } else {
                System.out.println("No result received yet, waiting...");
            }
        }

        verificationSocket.close();

        controller.stop();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Factoring completed. Success rate: " + counter_success + "/" + counter_total);
        System.out.println("Total duration: " + duration + " ms");
        // took x seconds per message
        long secondsPerMessage = duration / 1000 / numberOfMessages;
        System.out.println("Time taken per message: " + secondsPerMessage + " seconds");

        // stop workers by worker.stop()
        for (int i = 0; i < numberOfWorkers; i++) {
            workers[i].stop();
        }
        ctx.close();
    }

}
