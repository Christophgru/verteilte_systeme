import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class EchoServer {
    public static void main(String[] args) {
        String hostname = "vs.lxd-vs.uni-ulm.de";
        int port = 5678;
        String message = "Please provide a new expression!\n";

        try {
            // Resolve the hostname
            InetAddress serverAddress = InetAddress.getByName(hostname);

            // Create a UDP socket
            DatagramSocket socket = new DatagramSocket();

            // Prepare the outgoing message
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, port);

            // Send the packet
            socket.send(packet);
            System.out.println("Sent Message: " + message + " to " + hostname + " on port " + port);

            // Set a timeout for receiving (e.g., 2 seconds)
            socket.setSoTimeout(2000);

            // Prepare buffer for incoming data
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket response = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            String task = "";
            try {
                // Try to receive a response
                socket.receive(response);
                 task = new String(response.getData(), 0, response.getLength());
                System.out.println("Received reply: " + task);
            } catch (SocketTimeoutException e) {
                System.out.println("No response received after 1 second.");
            }
            //evaluate the expression, first multiply then addition
            int result = 0;
            //remove \n from
            task=task.trim();
            result =  evaluateExpression(task);
            String answer_to_server = task + "=" + result+ "\n";
            //send the result back to server
            byte[] answer_buffer = answer_to_server.getBytes();
            DatagramPacket answer_packet = new DatagramPacket(answer_buffer, answer_buffer.length, serverAddress, port);
            socket.send(answer_packet);
            System.out.println("Sent Message: " + answer_to_server + " to " + hostname + " on port " + port);

            response = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            String verification = "";
            try {
                // Try to receive a response
                socket.receive(response);
                 verification = new String(response.getData(), 0, response.getLength());
                System.out.println("Received reply: " + verification);
            } catch (SocketTimeoutException e) {
                System.out.println("No response received after 1 second.");
            }
            //print the result
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
        for (int p : products) total += p;
        return total;
    }

       
}
