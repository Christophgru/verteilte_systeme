package utils;

import java.net.Socket;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class tcp_socket extends abstract_socket {
    private Socket socket;
    private InetAddress address;
    private int port;

    public tcp_socket(String hostname, int port) throws Exception {
        this.address = InetAddress.getByName(hostname);
        this.port = port;
        this.socket = new Socket(address, port);
    }

    public void send(String message) throws Exception {
        byte[] buffer = message.getBytes();
        socket.getOutputStream().write(buffer);
    }

    // receive until sotimeout triggered
    public String receive() throws Exception {
        byte[] receiveBuffer = new byte[1024];
        try {
            socket.getInputStream().read(receiveBuffer);
        } catch (SocketTimeoutException e) {
            System.out.println("Socket timed out waiting for a response");
        }
        return new String(receiveBuffer).trim();
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSoTimeout(int timeout) throws Exception {
        socket.setSoTimeout(timeout);
    }
}
