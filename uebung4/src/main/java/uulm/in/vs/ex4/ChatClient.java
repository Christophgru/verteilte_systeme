package uulm.in.vs.ex4;

import io.grpc.ManagedChannelBuilder;

public class ChatClient {
    // constructor
    ManagedChannelBuilder<?> channelBuilder;
    io.grpc.ManagedChannel channel;
    ChatGrpc.ChatBlockingStub blockingStub;
    ChatGrpc.ChatStub asyncStub;

    public ChatClient(String address, int port) {
        // create channel to ChatServer and stubs
        channelBuilder = ManagedChannelBuilder.forAddress(address, port).usePlaintext();
        channel = channelBuilder.build();
        blockingStub = ChatGrpc.newBlockingStub(channel);
        asyncStub = ChatGrpc.newStub(channel);

    }

    public String login(String username) {
        // TODO
        LoginRequest request = LoginRequest.newBuilder()
                .setUsername(username)
                .build();
        LoginResponse response = blockingStub.login(request);
        return response.getSessionID();
    }

    public void logout(String username, String sessionID) {
        // TODO
        LogoutRequest request = LogoutRequest.newBuilder()
                .setUsername(username)
                .setSessionID(sessionID)
                .build();
        blockingStub.logout(request);
    }
}
