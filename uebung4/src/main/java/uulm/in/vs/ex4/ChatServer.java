package uulm.in.vs.ex4;

//mvn clean compile
//mvn exec:java -Dexec.mainClass="uulm.in.vs.ex4.ChatServer"
//./grpcwebproxy-v0.15.0-win64.exe --backend_addr=localhost:5555 --run_tls_server=false --server_http_debug_port=8080 --allow_all_origins
// cd /d/bin/uniulm/verteilte_systeme/uebung4/src/main && node server.js

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class ChatServer {
    // username -> sessionID
    private final static ConcurrentHashMap<String, String> users = new ConcurrentHashMap<>();
    // sessionID -> response observer for that client's chat stream (bidi or
    // browser)
    private final static ConcurrentHashMap<String, StreamObserver<ChatMessages>> sessions = new ConcurrentHashMap<>();
    private Server server;

    public static class ChatService extends ChatGrpc.ChatImplBase {

        @Override
        public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
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
                System.out.println("User " + username + " logged in with session token " + sessionToken);
            }
        }

        @Override
        public void logout(LogoutRequest request, StreamObserver<LogoutResponse> responseObserver) {
            String username = request.getUsername();
            String sessionToken = request.getSessionID();

            if (users.containsKey(username) && users.get(username).equals(sessionToken)) {
                // Remove user
                users.remove(username);
                StreamObserver<ChatMessages> chatObserver = sessions.remove(sessionToken);
                if (chatObserver != null) {
                    // This will cause onCompleted() to be called on the client side
                    chatObserver.onCompleted();
                }

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

        // ===== Original bidi-streaming RPC for non-web clients =====
        @Override
        public StreamObserver<ClientMessages> chatStream(StreamObserver<ChatMessages> responseObserver) {
            // Called once for every new client connection
            return new StreamObserver<ClientMessages>() {

                String currentSessionId = null;

                @Override
                public void onNext(ClientMessages value) {
                    // First message: only register this client's session, do not broadcast yet
                    if (currentSessionId == null) {
                        currentSessionId = value.getSessionID();
                        sessions.put(currentSessionId, responseObserver);
                        System.out.println("New ChatStream for Session: " + currentSessionId);
                        return;
                    }

                    String msg = value.getMessage();
                    System.out.println("[" + currentSessionId + "] says: " + msg);

                    // Broadcast to all connected chat sessions
                    ChatMessages broadcastMsg = ChatMessages.newBuilder()
                            .setStatus(StatusCode.OK)
                            .setMessage("[" + currentSessionId + "]: " + msg)
                            .build();

                    for (StreamObserver<ChatMessages> obs : sessions.values()) {
                        try {
                            obs.onNext(broadcastMsg);
                        } catch (RuntimeException e) {
                            // Ignore broken streams here
                        }
                    }
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Error in stream of session: " + currentSessionId + ": " + t.getMessage());
                    if (currentSessionId != null) {
                        sessions.remove(currentSessionId);
                    }
                }

                @Override
                public void onCompleted() {
                    System.out.println("Stream of session: " + currentSessionId + " ended.");
                    if (currentSessionId != null) {
                        sessions.remove(currentSessionId);
                    }
                    responseObserver.onCompleted();
                }
            };
        }

        // ===== New: browser-friendly server-streaming RPC =====
        @Override
        public void chatStreamBrowser(ChatStreamRequest request,
                StreamObserver<ChatMessages> responseObserver) {
            String sessionId = request.getSessionID();
            System.out.println("chatStreamBrowser called for session: " + sessionId);

            // Check session is valid (belongs to a logged-in user)
            boolean validSession = users.containsValue(sessionId);
            if (!validSession) {
                ChatMessages err = ChatMessages.newBuilder()
                        .setStatus(StatusCode.FAILED)
                        .setMessage("Invalid sessionID, please log in again.")
                        .build();
                responseObserver.onNext(err);
                responseObserver.onCompleted();
                return;
            }

            // Register this browser stream in the same sessions map
            sessions.put(sessionId, responseObserver);

            // Optional welcome message
            ChatMessages hello = ChatMessages.newBuilder()
                    .setStatus(StatusCode.OK)
                    .setMessage("Welcome to the chat, session " + sessionId + "!")
                    .build();
            responseObserver.onNext(hello);

            // IMPORTANT: do NOT call onCompleted() here; we keep the stream open
            // until client disconnects or logout() closes it.
        }

        // ===== New: browser-friendly unary "sendMessage" RPC =====
        @Override
        public void sendMessage(ClientMessages request,
                StreamObserver<ChatMessages> responseObserver) {
            String sessionId = request.getSessionID();
            String msg = request.getMessage();

            System.out.println("sendMessage from session " + sessionId + ": " + msg);

            boolean validSession = users.containsValue(sessionId);
            if (!validSession) {
                ChatMessages err = ChatMessages.newBuilder()
                        .setStatus(StatusCode.FAILED)
                        .setMessage("Invalid sessionID, cannot send message.")
                        .build();
                responseObserver.onNext(err);
                responseObserver.onCompleted();
                return;
            }

            // Broadcast to all connected streams (bidi + browser)
            ChatMessages broadcastMsg = ChatMessages.newBuilder()
                    .setStatus(StatusCode.OK)
                    .setMessage("[" + sessionId + "]: " + msg)
                    .build();

            for (StreamObserver<ChatMessages> obs : sessions.values()) {
                try {
                    obs.onNext(broadcastMsg);
                } catch (RuntimeException e) {
                    // Ignore broken streams
                }
            }

            // Unary ACK to the sender
            ChatMessages ack = ChatMessages.newBuilder()
                    .setStatus(StatusCode.OK)
                    .setMessage("Message delivered.")
                    .build();
            responseObserver.onNext(ack);
            responseObserver.onCompleted();
        }

        @Override
        public void listUsers(GetUsersMessage request, StreamObserver<UserInfoMessage> responseObserver) {
            String sessionId = request.getSessionID();

            // Check if the given session ID belongs to any logged-in user
            boolean validSession = users.containsValue(sessionId);

            UserInfoMessage.Builder builder = UserInfoMessage.newBuilder();

            if (!validSession) {
                // Invalid session: return FAILED and an empty user list
                builder.setStatus(StatusCode.FAILED);
            } else {
                // Valid session: return OK and all currently logged-in usernames
                builder.setStatus(StatusCode.OK);
                builder.addAllUser(users.keySet()); // users: username -> sessionID
            }

            UserInfoMessage response = builder.build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.startServerAsync(5555);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
        System.out.println("Shutting down Chat Server...");
        server.stopServer();

    }

    public void stopServer() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void startServerAsync(int port) {
        System.out.println("Starting Chat Server...");
        try {
            server = ServerBuilder.forPort(port)
                    .addService(new ChatService())
                    .build()
                    .start();

            Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));

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

    // NEW: helper for tests so static state does not leak between tests
    public static void resetState() {
        users.clear();
        sessions.clear();
    }
}
