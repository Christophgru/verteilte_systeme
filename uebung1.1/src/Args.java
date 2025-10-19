package src;

public class Args {
    public String host;
    public int port;
    public Protocol protocol;
    public String msg;

    public Args(String[] args) {
        if (args.length < 3 || args.length > 4) {
            System.err.println("Usage: java TcpSender <host> <port> optional: <tcp/udp> [message]");
            System.exit(1);
        }

        this.host = args[0];
        this.port = Integer.parseInt(args[1]);

        if (args.length == 4) {
            switch (args[2]) {
                case "tcp":
                    this.protocol = Protocol.TCP;
                    break;

                case "udp":
                    this.protocol = Protocol.UDP;
                    break;

                default:
                    System.err.println(
                            "Usage: java TcpSender <host> <port> optional: <tcp/udp> [message] \n Provided protocol is neither udp, nor tcp!");
                    System.exit(1);
            }
            this.msg = args[3];
        } else {
            this.msg = args[2];
            this.protocol = Protocol.TCP;
        }
    }
}
