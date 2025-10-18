import java.util.ArrayList;
import java.util.List;

import utils.tcp_socket;

public class CalculationServer {
    public static void main(String[] args) {
        System.out.println("Calculation Client started.");
        String hostname = "vs.lxd-vs.uni-ulm.de";
        int port = 5678;
        String message = "Please provide a new expression!\n";

        try {
            // Create a TCP socket
            tcp_socket socket = new tcp_socket(hostname, port);
            socket.setSoTimeout(2000);

            // Prepare the outgoing message

            // Send the packet
            socket.send(message);
            System.out.println("Sent Message: " + message + " to " + hostname + " on port " + port);

            // Prepare buffer for incoming data
            String task = socket.receive();
            System.out.println("Received reply: " + task);

            // remove \n from
            task = task.trim();
            // evaluate the expression, first multiply then addition
            int result = evaluateExpression(task);
            // build respoinse message
            String answer_to_server = task + "=" + result + "\n";
            // send the result back to server
            socket.send(answer_to_server);
            System.out.println("Sent Message: " + answer_to_server + " to " + hostname + " on port " + port);

            String verification = socket.receive();
            System.out.println("Received reply: " + verification);

            // print the result
            System.out.println(result);
            // Close the socket
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
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
