import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class EchoServer {
    public static void main(String[] args) {
        String hostname = "vs.lxd-vs.uni-ulm.de";
        int port = 3211;
        String message = "Hello from Java UDP client!";

        try {
            // Resolve the hostname
            InetAddress serverAddress = InetAddress.getByName(hostname);

            // Create a UDP socket
            DatagramSocket socket = new Socket();

            // Prepare the outgoing message
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, port);

            // Send the packet
            socket.send(packet);
            System.out.println("UDP message sent to " + hostname + " on port " + port);

            // Wait 1 second before checking for response
            Thread.sleep(1000);

            // Set a timeout for receiving (e.g., 2 seconds)
            socket.setSoTimeout(2000);

            // Prepare buffer for incoming data
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket response = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            try {
                // Try to receive a response
                socket.receive(response);
                String reply = new String(response.getData(), 0, response.getLength());
                System.out.println("Received reply: " + reply);
            } catch (SocketTimeoutException e) {
                System.out.println("No response received after 1 second.");
            }

            // Close the socket
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
