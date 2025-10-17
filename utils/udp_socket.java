package utils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class udp_socket extends abstract_socket {
    private DatagramSocket socket;
    private InetAddress address;
    private int port;

    public udp_socket(String hostname, int port) throws Exception {
        this.address = InetAddress.getByName(hostname);
        this.port = port;
        this.socket = new DatagramSocket();
    }

    public void send(String message) throws Exception {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(packet);
    }

    // receive until sotimeout triggered
    public String receive() throws Exception {
        byte[] receiveBuffer = new byte[1024];
        DatagramPacket response = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        try {
            socket.receive(response);
        } catch (SocketTimeoutException e) {
            System.out.println("Socket timed out waiting for a response");
        }
        return new String(response.getData(), 0, response.getLength());
    }

    public void close() {
        socket.close();
    }

    public void setSoTimeout(int timeout) throws Exception {
        socket.setSoTimeout(timeout);
    }
}
