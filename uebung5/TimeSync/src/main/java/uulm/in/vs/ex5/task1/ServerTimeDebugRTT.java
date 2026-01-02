package uulm.in.vs.ex5.task1;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.nio.charset.StandardCharsets;

public class ServerTimeDebugRTT {

    public static void main(String[] args) {
        String host = "vs.lxd-vs.uni-ulm.de:3322";

        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.REQ);
            socket.connect("tcp://" + host);

            for (int i = 0; i < 10; i++) {
                long t0 = System.currentTimeMillis();

                socket.send(new byte[0]); // oder "TIME"
                byte[] reply = socket.recv(0);

                long t1 = System.currentTimeMillis();

                if (reply == null) {
                    System.out.println("No reply");
                    continue;
                }

                String response = new String(reply, StandardCharsets.UTF_8).trim();
                long rtt = t1 - t0;

                System.out.printf(
                        "Response: %s | RTT = %d ms%n",
                        response, rtt
                );

                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
