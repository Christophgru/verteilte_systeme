package uulm.in.vs.ex4;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ChatClient {

    private final ManagedChannel channel;
    private final ChatGrpc.ChatBlockingStub blockingStub;
    private final ChatGrpc.ChatStub asyncStub;

    private StreamObserver<ClientMessages> chatRequestObserver;
    private String currentSessionId;

    // Queue for messages coming from the server
    private final BlockingQueue<ChatMessages> incomingMessages = new LinkedBlockingQueue<>();

    public ChatClient(String address, int port) {
        this.channel = ManagedChannelBuilder
                .forAddress(address, port)
                .usePlaintext()
                .build();
        this.blockingStub = ChatGrpc.newBlockingStub(channel);
        this.asyncStub = ChatGrpc.newStub(channel);
    }

    public String login(String username) {
        LoginRequest request = LoginRequest.newBuilder()
                .setUsername(username)
                .build();
        LoginResponse response = blockingStub.login(request);
        return response.getSessionID();
    }

    public void logout(String username, String sessionID) {
        LogoutRequest request = LogoutRequest.newBuilder()
                .setUsername(username)
                .setSessionID(sessionID)
                .build();
        blockingStub.logout(request);
    }

    /**
     * Starts a bidirectional chat stream with the server.
     */
    public void startChatStream(String sessionID) {
        this.currentSessionId = sessionID;

        StreamObserver<ChatMessages> responseObserver = new StreamObserver<ChatMessages>() {
            @Override
            public void onNext(ChatMessages value) {
                incomingMessages.add(value);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error in chat stream: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Server closed the chat stream.");
            }
        };

        chatRequestObserver = asyncStub.chatStream(responseObserver);
    }

    /**
     * Sends a message on the open chat stream.
     * The first message is used by the server to register this client.
     */
    public void sendChatMessage(String message) {
        if (chatRequestObserver == null) {
            throw new IllegalStateException("Chat stream is not started. Call startChatStream() first.");
        }
        if (currentSessionId == null) {
            throw new IllegalStateException("No session ID set. Call login() first.");
        }

        ClientMessages clientMessage = ClientMessages.newBuilder()
                .setSessionID(currentSessionId)
                .setMessage(message)
                .build();

        chatRequestObserver.onNext(clientMessage);
    }

    public void stopChatStream() {
        if (chatRequestObserver != null) {
            chatRequestObserver.onCompleted();
            chatRequestObserver = null;
        }
    }

    public ChatMessages waitForNextMessage(long timeout, TimeUnit unit) throws InterruptedException {
        return incomingMessages.poll(timeout, unit);
    }

    public void clearIncomingMessages() {
        incomingMessages.clear();
    }

    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }
}
