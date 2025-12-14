package uulm.in.vs.ex4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;

public class ChatServer {
    // username -> sessionID
    private final static ConcurrentHashMap<String, String> users = new ConcurrentHashMap<>();
    // sessionID -> username
    private final static ConcurrentHashMap<String, String> sessionToUser = new ConcurrentHashMap<>();
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
                sessionToUser.put(sessionToken, username);

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
                sessionToUser.remove(sessionToken);

                StreamObserver<ChatMessages> chatObserver = sessions.remove(sessionToken);
                if (chatObserver != null) {
                    try {
                        chatObserver.onCompleted();
                    } catch (Exception e) {
                        System.err
                                .println("Error completing stream for session " + sessionToken + ": " + e.getMessage());
                    }
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
            return new StreamObserver<ClientMessages>() {

                String currentSessionId = null;

                @Override
                public void onNext(ClientMessages value) {
                    if (currentSessionId == null) {
                        currentSessionId = value.getSessionID();
                        sessions.put(currentSessionId, responseObserver);
                        System.out.println("New ChatStream (bidi) for session: " + currentSessionId
                                + " (total sessions = " + sessions.size() + ")");
                        return;
                    }

                    String msg = value.getMessage();
                    String username = sessionToUser.getOrDefault(currentSessionId, "unknown");
                    System.out.println("[" + currentSessionId + " / " + username + "] says (bidi): " + msg);

                    ChatMessages broadcastMsg = ChatMessages.newBuilder()
                            .setStatus(StatusCode.OK)
                            .setMessage("[" + username + "]: " + msg)
                            .build();

                    // broadcast safely
                    List<String> toRemove = new ArrayList<>();
                    for (var entry : sessions.entrySet()) {
                        String sid = entry.getKey();
                        StreamObserver<ChatMessages> obs = entry.getValue();
                        try {
                            obs.onNext(broadcastMsg);
                        } catch (Exception e) {
                            System.err.println("Removing broken session " + sid + " due to error: " + e.getMessage());
                            toRemove.add(sid);
                        }
                    }
                    toRemove.forEach(sessions::remove);
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Error in bidi stream of session: " + currentSessionId + ": " + t);
                    if (currentSessionId != null) {
                        sessions.remove(currentSessionId);
                    }
                }

                @Override
                public void onCompleted() {
                    System.out.println("Bidi stream of session: " + currentSessionId + " ended.");
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
            String username = sessionToUser.get(sessionId);
            System.out.println("chatStreamBrowser called for session: " + sessionId + ", user=" + username);

            if (username == null) {
                ChatMessages err = ChatMessages.newBuilder()
                        .setStatus(StatusCode.FAILED)
                        .setMessage("Invalid sessionID, please log in again.")
                        .build();
                responseObserver.onNext(err);
                responseObserver.onCompleted();
                return;
            }

            // We need the server-side variant to detect cancellation
            ServerCallStreamObserver<ChatMessages> serverObserver = (ServerCallStreamObserver<ChatMessages>) responseObserver;

            // Remove this session when the client disconnects / stream is cancelled
            serverObserver.setOnCancelHandler(() -> {
                System.out.println("Browser stream cancelled for session: " + sessionId);
                sessions.remove(sessionId);
            });

            sessions.put(sessionId, serverObserver);
            System.out.println("Registered browser stream for session: " + sessionId
                    + " (total sessions = " + sessions.size() + ")");

            ChatMessages hello = ChatMessages.newBuilder()
                    .setStatus(StatusCode.OK)
                    .setMessage("Welcome to the chat, " + username + "!")
                    .build();
            serverObserver.onNext(hello);

            // do NOT call onCompleted(); keep stream open
        }

        // ===== New: browser-friendly unary "sendMessage" RPC =====
        @Override
        public void sendMessage(ClientMessages request,
                StreamObserver<ChatMessages> responseObserver) {
            String sessionId = request.getSessionID();
            String msg = request.getMessage();
            String username = sessionToUser.get(sessionId);

            System.out.println("sendMessage from session " + sessionId + " / user=" + username
                    + ": \"" + msg + "\" (sessions=" + sessions.size() + ")");

            if (username == null) {
                ChatMessages err = ChatMessages.newBuilder()
                        .setStatus(StatusCode.FAILED)
                        .setMessage("Invalid sessionID, cannot send message.")
                        .build();
                responseObserver.onNext(err);
                responseObserver.onCompleted();
                return;
            }

            ChatMessages broadcastMsg = ChatMessages.newBuilder()
                    .setStatus(StatusCode.OK)
                    .setMessage("[" + username + "]: " + msg)
                    .build();

            // broadcast safely
            List<String> toRemove = new ArrayList<>();
            for (var entry : sessions.entrySet()) {
                String sid = entry.getKey();
                StreamObserver<ChatMessages> obs = entry.getValue();
                try {
                    obs.onNext(broadcastMsg);
                } catch (Exception e) {
                    System.err.println("Removing broken session " + sid + " due to error: " + e.getMessage());
                    toRemove.add(sid);
                }
            }
            toRemove.forEach(sessions::remove);

            ChatMessages ack = ChatMessages.newBuilder()
                    .setStatus(StatusCode.OK)
                    .setMessage("Message delivered.")
                    .build();
            responseObserver.onNext(ack);
            responseObserver.onCompleted();
        }

        @Override
        public void listUsers(GetUsersMessage request, StreamObserver<UserInfoMessage> responseObserver) {
            try {
                String sessionId = request.getSessionID();
                System.out.println("listUsers called with sessionId='" + sessionId + "'");

                boolean validSession = sessionToUser.containsKey(sessionId);
                UserInfoMessage.Builder builder = UserInfoMessage.newBuilder();

                if (!validSession) {
                    System.out.println("listUsers: invalid sessionId");
                    builder.setStatus(StatusCode.FAILED);
                } else {
                    builder.setStatus(StatusCode.OK);

                    // copy to avoid any weird concurrent-iteration issues
                    List<String> currentUsers = new ArrayList<>(users.keySet());
                    System.out.println("listUsers: returning users = " + currentUsers);
                    builder.addAllUser(currentUsers);
                }

                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
            } catch (Exception e) {
                e.printStackTrace(); // this is what we want to see in the server console
                responseObserver.onError(
                        io.grpc.Status.INTERNAL
                                .withDescription("listUsers failed: " + e.getMessage())
                                .withCause(e)
                                .asRuntimeException());
            }
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

    public static void resetState() {
        users.clear();
        sessionToUser.clear();
        sessions.clear();
    }
}
