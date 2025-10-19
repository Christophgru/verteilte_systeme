package src;

public abstract class AbstractSender {
    protected String host;
    protected int port;

    public abstract void echo(String msg);

    protected static void abort(String msg, Exception e) {
        if (e != null && e.getMessage() != null && !e.getMessage().isBlank()) {
            System.err.println(msg + " Cause: " + e.getMessage());
        } else {
            System.err.println(msg);
        }

        System.exit(1);
    }
}
