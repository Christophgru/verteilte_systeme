package utils;

import java.net.Socket;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class tcp_socket extends abstract_socket {
    private Socket socket;

    public tcp_socket(String hostname, int port) throws Exception {
        this.address = InetAddress.getByName(hostname);
        this.socket = new Socket(address, port);
        isConnected = true;
    }

    public void send(String message) throws Exception {
        isBusy = true;
        byte[] buffer = message.getBytes();
        socket.getOutputStream().write(buffer);
        isBusy = false;
    }

    // receive until sotimeout triggered
    public String receive() throws Exception {
        isBusy = true;
        byte[] receiveBuffer = new byte[1024];
        try {
            socket.getInputStream().read(receiveBuffer);
        } catch (SocketTimeoutException e) {
            System.out.println("Socket timed out waiting for a response");
        }
        isBusy = false;
        return new String(receiveBuffer).trim();
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        isConnected = false;
    }

    public void setTimeout(int timeout) throws Exception {
        socket.setSoTimeout(timeout);
    }

}
