public class Echo_Client {
    public static void main(String[] args) throws Exception {
        Args arguments = new Args(args);
        AbstractSender sender = null;
        switch (arguments.protocol) {
            case TCP:
                sender = new TcpSender(arguments);

                break;
        
            case UDP:
                sender = new UdpSender(arguments);

                break;
        }
        sender.echo();
    }
}
