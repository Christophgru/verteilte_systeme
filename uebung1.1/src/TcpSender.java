package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class TcpSender extends AbstractSender {
    public TcpSender(Args args) {
        this.host = args.host;
        this.port = args.port;
    }

    public void echo(String msg) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(this.host, this.port), 5000);
            socket.setSoTimeout(5000);

            OutputStream rawOut = socket.getOutputStream();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(rawOut, StandardCharsets.UTF_8));
            out.write(msg);
            out.newLine();
            out.flush();

            System.out.printf("Sent via TCP: %s%n", msg);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("SERVER: " + line);
            }

        } catch (UnknownHostException e) {
            abort("Unknown host: " + this.host, e);
        } catch (ConnectException e) {
            abort("Connection failed to " + this.host + ":" + this.port, e);
        } catch (SocketTimeoutException e) {
            abort("Timeout while waiting for response.", e);
        } catch (IOException e) {
            abort("I/O error during TCP communication.", e);
        } catch (SecurityException e) {
            abort("Security error (permissions?).", e);
        } catch (RuntimeException e) {
            abort("Unexpected runtime error.", e);
        }
    }
}
