package utils;

public abstract class abstract_socket {
    public abstract void send(String message) throws Exception;

    public abstract String receive() throws Exception;

    public abstract void close();

    public abstract void setSoTimeout(int timeout) throws Exception;
}
