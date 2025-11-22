package uulm.in.vs.ex4;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class ChatServer {
    private final static ConcurrentHashMap<String, String> users = new ConcurrentHashMap<>();
    private Server server;

    public static class ChatService extends ChatGrpc.ChatImplBase {
        @Override
        public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
            // TODO if username is not taken return OK with random session token
            // otherwise return FAILED
            String username = request.getUsername();
            if (users.containsKey(username)) {
                LoginResponse response = LoginResponse.newBuilder()
                        .setStatus(StatusCode.FAILED)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                String sessionToken = java.util.UUID.randomUUID().toString();
                users.put(username, sessionToken);
                LoginResponse response = LoginResponse.newBuilder()
                        .setStatus(StatusCode.OK)
                        .setSessionID(sessionToken)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                // store username and session token
                System.out.println("User " + username + " logged in with session token " + sessionToken);
            }
        }

        @Override
        public void logout(LogoutRequest request, StreamObserver<LogoutResponse> responseObserver) {
            // TODO
            String username = request.getUsername();
            String sessionToken = request.getSessionID();
            if (users.containsKey(username) && users.get(username).equals(sessionToken)) {
                users.remove(username);
                LogoutResponse response = LogoutResponse.newBuilder()
                        .setStatus(StatusCode.OK)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                System.out.println("User " + username + " logged out.");
            } else {
                LogoutResponse response = LogoutResponse.newBuilder()
                        .setStatus(StatusCode.FAILED)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        }

    }

    public static void main(String[] args) {

        ChatServer server = new ChatServer();
        server.startServerAsync(5555);
        // sleep 20 seconds to keep server alive for testing
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Shutting down Chat Server...");
    }

    public void stopServer() {
        // This method can be implemented to stop the server gracefully if needed
        server.shutdown();
    }

    public void startServerAsync(int port) {
        System.out.println("Starting Chat Server...");
        try {
            // Create and start the server
            server = ServerBuilder.forPort(port)
                    .addService(new ChatService())
                    .build()
                    .start();

            // Add a hook to shut the server down if the program is terminated
            Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));

            // Wait for the server to terminate
            // satrt thread to not block main thread
            new Thread(() -> {
                try {
                    server.awaitTermination();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getConnectedUsersCount() {
        return users.size();
        }
}
