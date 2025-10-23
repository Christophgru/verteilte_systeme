package utils;

import java.net.InetAddress;

public abstract class abstract_socket {
    protected InetAddress address;
    protected int port;

    protected boolean isConnected = false;
    protected boolean isBusy = false;
    protected boolean inUse = false;

    public abstract void send(String message) throws Exception;

    public abstract String receive() throws Exception;

    public abstract void close();

    public abstract void setTimeout(int timeout) throws Exception;

    public boolean isConnected() {
        return isConnected;
    }

    public boolean isBusy() {
        return isBusy;
    }

    public void setInUse(boolean b) {
        inUse = b;
    };

    public boolean isFree() {
        return !isBusy && !inUse;
    }

}
