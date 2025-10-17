import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import utils.abstract_socket;
import utils.tcp_socket;
import utils.udp_socket;

public class CalculationServer {
    public static void main(String[] args) {
        System.out.println("Calculation Client started.");
        String hostname = "vs.lxd-vs.uni-ulm.de";
        int port = 5678;
        abstract_socket socket;
        System.out.println("Choose wich type of socket you want to use (tcp/udp): Press 1 for tcp, 2 for udp");
        try {
            int choice = System.in.read();
            if (choice == 1) {
                socket = new tcp_socket(hostname, port);
            } else {
                // Handle UDP case
                socket = new udp_socket(hostname, port); // Placeholder for UDP socket
            }
        } catch (Exception e) {
            // TODO: handle exception
            return;
        }
        int success_counter = 0;
        int failure_counter = 0;
        // get the current time to calculate fps
        long startTime = System.currentTimeMillis();
        try {
            for (int i = 0; i < 500; i++) {
                if (!runCalculationClient(socket)) {
                    System.out.println("Calculation failed.");
                    failure_counter++;
                } else {
                    success_counter++;
                }
            }
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            socket.close();
            e1.printStackTrace();
        }
        socket.close();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double fps = 500.0 / (duration / 1000.0);
        System.out.println("FPS: " + fps);
        System.out.println(
                "Calculation Client finished. Successes: " + success_counter + ", Failures: " + failure_counter);

    }

    private static boolean runCalculationClient(abstract_socket socket) {
        try {
            String message = "Please provide a new expression!\n";

            socket.setSoTimeout(2000);
            socket.send(message);
            System.out.println("Message sent to " + socket + " on port " + message);

            // Prepare buffer for incoming data
            String response = socket.receive().trim();
            int result = 0;
            if (response.isEmpty()) {
                System.out.println("No response received after 1 second.");
            } else {
                System.out.println("Received reply: " + response);
                result = evaluateExpression(response);
                System.out.println("Evaluation result: " + result);
            }
            String answer_to_server = response + "=" + result + "\n";
            // send the result back to server
            socket.send(answer_to_server);
            System.out.println("Sent Message: " + answer_to_server);

            String verification = socket.receive();

            System.out.println("Received reply: " + verification);
            if (verification.contains("correct")) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static int evaluateExpression(String expression) {
        // Split by '+' first
        String[] sumParts = expression.split("\\+");
        for (String s : sumParts) {
            System.out.println("Sum part: " + s);
        }

        List<Integer> products = new ArrayList<>();
        for (String part : sumParts) {
            // Each part can be a chain of multiplications
            String[] factors = part.split("\\*");
            for (String s : factors) {
                System.out.println("Factor: " + s);
            }

            int product = 1;
            for (String f : factors) {
                f = f.trim();
                if (f.isEmpty()) {
                    throw new NumberFormatException("Empty factor in: " + expression);
                }
                product *= Integer.parseInt(f);
            }
            products.add(product);
        }

        int total = 0;
        for (int p : products)
            total += p;
        return total;
    }

}
