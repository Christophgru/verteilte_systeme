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
        this.arguments = args;
    }

    public void echo() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(this.arguments.host, this.arguments.port), 5000);
            socket.setSoTimeout(5000);

            OutputStream rawOut = socket.getOutputStream();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(rawOut, StandardCharsets.UTF_8));
            out.write(this.arguments.msg);
            out.newLine();
            out.flush();

            System.out.printf("Sent via TCP: %s%n", this.arguments.msg);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("SERVER: " + line);
            }

        } catch (UnknownHostException e) {
            abort("Unknown host: " + this.arguments.host, e);
        } catch (ConnectException e) {
            abort("Connection failed to " + this.arguments.host + ":" + this.arguments.port, e);
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
