public abstract class AbstractSender {
    protected Args arguments;
    public abstract void echo ();
    protected static void abort(String msg, Exception e) {
        if (e != null && e.getMessage() != null && !e.getMessage().isBlank()) {
            System.err.println(msg + " Cause: " + e.getMessage());
        } else {
            System.err.println(msg);
        }

        System.exit(1);
    }
}
