
import utils.udp_socket;

public class EchoServer {
    public static void main(String[] args) {
        String hostname = "vs.lxd-vs.uni-ulm.de";
        int port = 3211;
        String message = "Echo!";

        try {
            // Resolve the hostname
            udp_socket serverAddress = new udp_socket(hostname, port);
            serverAddress.setSoTimeout(2000);

            // Send the packet
            serverAddress.send(message);
            System.out.println("UDP message sent to " + hostname + " on port " + port);

            // Wait 1 second before checking for response

            // Prepare buffer for incoming data
            String response = serverAddress.receive();

            System.out.println("Received reply: " + response);

            // Close the socket
            serverAddress.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
