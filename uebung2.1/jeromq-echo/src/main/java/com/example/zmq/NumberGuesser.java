package com.example.zmq;

import java.util.Arrays;

import com.example.zmq.net.JeromqReqSocket;

//compile from jeromq-echo using 
//mvn -q -DskipTests package
//run: java -cp target/jeromq-echo-1.0-SNAPSHOT-shaded.jar com.example.zmq.NumberGuesser
enum resultType {
    TOO_SMALL,
    TOO_LARGE,
    CORRECT,
    UNKNOWN
}

public class NumberGuesser {
    // Usage: EchoClient tcp://host:port "your message"
    public static void main(String[] args) {
        String endpoint = "tcp://vs.lxd-vs.uni-ulm.de:27401";
        long gameId = 0;
        long guess = (long) Math.pow(2, 63);
        long stepsize = (long) Math.pow(2, 62);
        int timeoutMs = 1000;
        String reply;
        try (JeromqReqSocket sock = new JeromqReqSocket(endpoint, timeoutMs)) {
            reply = sock.request(String.format("%s:%s", String.valueOf(gameId), String.valueOf(guess)));
            // get a int number from the answer "GameID unknown! The current gameID is
            // 3736278849413228172"
            String[] parts = reply.split(" ");
            System.out.println(Arrays.toString(parts));
            String id_string = parts[parts.length - 1];
            System.out.println(id_string);
            gameId = Long.parseLong(id_string);
            System.out.println("The gameID is: " + gameId);
            resultType result = resultType.UNKNOWN;
            long attempts_too_small = 0;
            long attempts_too_large = 0;
            while (result != resultType.CORRECT) {
                reply = sendGuess(sock, String.valueOf(gameId), String.valueOf(guess), timeoutMs);

                result = evaluateReply(reply);
                if (result == resultType.CORRECT &&attempts_too_large == 0 && attempts_too_small == 0) {
                    System.out.println("Guessed the number on the first try!");
                    return;
                }
                if (null == result) {
                    System.out.println("Received unknown result, exiting.");
                    return;
                } else switch (result) {
                    case TOO_SMALL ->                         {
                            long old_guess = guess;
                            guess = guess + stepsize + 1;
                            attempts_too_small += 1;
                            stepsize = stepsize / 2;
                                 
                            System.out.println("Adjusting guess from " + old_guess + " to " + guess);
                            if(attempts_too_small > 70){
                                // increase stepsize again
                                guess=(long) Math.pow(2, 63);
                                stepsize = (long) Math.pow(2, 62);
                            }  
                        }
                    case TOO_LARGE ->                         {
                            long old_guess = guess;
                            guess = guess - stepsize + 1;
                            stepsize = stepsize / 2;
                            attempts_too_large += 1;
                            
                            System.out.println("Adjusting guess from " + old_guess + " to " + guess);
                             if(attempts_too_large > 70){
                                // increase stepsize again
                                guess = (long) Math.pow(2, 63);
                                stepsize = (long) Math.pow(2, 62);
                            }
                        }
                    default -> {
                        System.out.println("Received unknown result, exiting.");
                        return;
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static resultType evaluateReply(String reply) {
        if (reply.contains("too small")) {
            System.out.println("The guessed number is too small.");
            return resultType.TOO_SMALL;
        } else if (reply.contains("too large")) {
            System.out.println("The guessed number is too large.");
            return resultType.TOO_LARGE;
        } else if (reply.contains("Correct guess after")) {
            System.out.println("The guessed number is correct!");
            return resultType.CORRECT;
        } else {
            System.out.println("Unexpected reply: " + reply);
            return resultType.UNKNOWN;
        }
    }

    public static String sendGuess(JeromqReqSocket socket, String gameId, String guess, int timeoutMs) {
        String reply = null;
        try {
            String request = String.format("%s:%s", gameId, guess);
            System.out.println("→ Sending: " + request);
            reply = socket.request(request);
            if (reply == null) {
                System.err.println("No reply within " + timeoutMs + " ms");
                System.exit(2);
            } else {
                System.out.println("Received: " + reply);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reply;
    }
}
