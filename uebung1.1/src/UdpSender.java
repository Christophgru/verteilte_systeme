package src;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.BindException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class UdpSender extends AbstractSender {
    public UdpSender(Args args) {
        this.host = args.host;
        this.port = args.port;
    }

    public void echo(String msg) {
        try {
            InetAddress addr = InetAddress.getByName(this.host);
            byte[] data = msg.getBytes(StandardCharsets.UTF_8);

            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(5000);

                DatagramPacket send = new DatagramPacket(data, data.length, addr, this.port);
                socket.send(send);
                System.out.printf("Sent via UDP: %s%n", msg);

                byte[] buf = new byte[8192];
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(recv);
                    String resp = new String(
                            recv.getData(), recv.getOffset(), recv.getLength(), StandardCharsets.UTF_8);
                    System.out.println("SERVER: " + resp);
                } catch (SocketTimeoutException e) {
                    abort("Timeout while waiting for response.", e);
                }
            }
        } catch (UnknownHostException e) {
            abort("Unknown host: " + this.host, e);
        } catch (PortUnreachableException e) {
            abort("Port unreachable at " + this.host + ":" + this.port, e);
        } catch (BindException e) {
            abort("Failed to bind local UDP socket (address/port in use?).", e);
        } catch (SecurityException e) {
            abort("Security error (permissions?).", e);
        } catch (IOException e) {
            abort("I/O error during UDP communication.", e);
        } catch (RuntimeException e) {
            abort("Unexpected runtime error.", e);
        }
    }
}
