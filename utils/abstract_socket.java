package utils;

import java.net.InetAddress;

public abstract class abstract_socket {
    protected InetAddress address;
    protected int port;

    public abstract void send(String message) throws Exception;

    public abstract String receive() throws Exception;

    public abstract void close();

    public abstract void setTimeout(int timeout) throws Exception;
}
