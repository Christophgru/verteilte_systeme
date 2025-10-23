package utils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class udp_socket extends abstract_socket {
    private DatagramSocket socket;

    public udp_socket(String hostname, int port) throws Exception {
        this.address = InetAddress.getByName(hostname);
        this.port = port;
        this.socket = new DatagramSocket();
        isConnected = true;
    }

    public void send(String message) throws Exception {
        isBusy = true;
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(packet);
        isBusy = false;
    }

    // receive until sotimeout triggered
    public String receive() throws Exception {
        isBusy = true;
        byte[] receiveBuffer = new byte[1024];
        DatagramPacket response = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        try {
            socket.receive(response);
        } catch (SocketTimeoutException e) {
            System.out.println("Socket timed out waiting for a response");
        }
        isBusy = false;
        return new String(response.getData(), 0, response.getLength());
    }

    public void close() {
        socket.close();
        isConnected = false;
    }

    public void setTimeout(int timeout) throws Exception {
        socket.setSoTimeout(timeout);
    }
}
