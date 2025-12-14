package uulm.in.vs.ex4;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ChatClient {

    private final ManagedChannel channel;
    private final ChatGrpc.ChatBlockingStub blockingStub;
    private final ChatGrpc.ChatStub asyncStub;

    // Stored logged-in user information
    private String username;
    private String sessionId;

    // Chat stream state
    private StreamObserver<ClientMessages> chatRequestObserver;
    private volatile boolean chatStreamCompleted = false;

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

    /**
     * Logs in the given username, stores username and sessionId internally
     * and returns the sessionId for convenience.
     */
    public String login(String username) {
        LoginRequest request = LoginRequest.newBuilder()
                .setUsername(username)
                .build();
        LoginResponse response = blockingStub.login(request);

        if (response.getStatus() != StatusCode.OK) {
            throw new IllegalStateException("Login failed for user: " + username);
        }

        this.username = username;
        this.sessionId = response.getSessionID();
        return this.sessionId;
    }

    /**
     * Logs out the currently stored user.
     */
    public void logout() {
        if (username == null || sessionId == null) {
            throw new IllegalStateException("No user is logged in.");
        }

        LogoutRequest request = LogoutRequest.newBuilder()
                .setUsername(username)
                .setSessionID(sessionId)
                .build();
        LogoutResponse response = blockingStub.logout(request);

        if (response.getStatus() != StatusCode.OK) {
            throw new IllegalStateException("Logout failed for user: " + username);
        }

        this.username = null;
        this.sessionId = null;
    }

    /**
     * Starts a bidirectional chat stream using the internally stored sessionId.
     */
    public void startChatStream() {
        if (sessionId == null) {
            throw new IllegalStateException("Cannot start chat stream without a logged-in session.");
        }

        this.chatStreamCompleted = false;

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
                chatStreamCompleted = true;
                chatRequestObserver = null;
            }
        };

        chatRequestObserver = asyncStub.chatStream(responseObserver);

        // First message can be used to register this session on the server side
        sendChatMessage("REGISTER-STREAM");
    }

    /**
     * Sends a chat message using the current sessionId.
     */
    public void sendChatMessage(String message) {
        if (chatRequestObserver == null) {
            throw new IllegalStateException("Chat stream is not started. Call startChatStream() first.");
        }
        if (sessionId == null) {
            throw new IllegalStateException("No session ID set. Call login() first.");
        }

        ClientMessages clientMessage = ClientMessages.newBuilder()
                .setSessionID(sessionId)
                .setMessage(message)
                .build();

        chatRequestObserver.onNext(clientMessage);
    }

    public void stopChatStream() {
        if (chatRequestObserver != null && !chatStreamCompleted) {
            chatRequestObserver.onCompleted();
            chatRequestObserver = null;
        }
    }

    public boolean isChatStreamCompleted() {
        return chatStreamCompleted;
    }

    public ChatMessages waitForNextMessage(long timeout, TimeUnit unit) throws InterruptedException {
        return incomingMessages.poll(timeout, unit);
    }

    public void clearIncomingMessages() {
        incomingMessages.clear();
    }

    /**
     * Calls listUsers using the internally stored sessionId.
     */
    public List<String> listUsers() {
        if (sessionId == null) {
            throw new IllegalStateException("Cannot list users without a logged-in session.");
        }

        GetUsersMessage request = GetUsersMessage.newBuilder()
                .setSessionID(sessionId)
                .build();

        UserInfoMessage response = blockingStub.listUsers(request);
        if (response.getStatus() != StatusCode.OK) {
            throw new IllegalStateException("listUsers failed with status: " + response.getStatus());
        }

        return response.getUserList();
    }

    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }

    // Optional getters if you need them
    public String getUsername() {
        return username;
    }

    public String getSessionId() {
        return sessionId;
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient("localhost", 5555);
        try {
            String sessionId = client.login("exampleUser");
            System.out.println("Logged in with session ID: " + sessionId);

            client.startChatStream();
            client.sendChatMessage("Hello, World!");

            ChatMessages message = client.waitForNextMessage(5, TimeUnit.SECONDS);
            if (message != null) {
                System.out.println("Received message: " + message.getMessage());
            } else {
                System.out.println("No message received within timeout.");
            }

            client.stopChatStream();
            client.logout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
