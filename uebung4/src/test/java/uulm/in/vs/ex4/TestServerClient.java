package uulm.in.vs.ex4;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class TestServerClient {

    ChatServer server;
    ChatClient client;

    @Test
    public void testLoginLogout() {
        System.out.println("Starting testLoginLogout...");

        ChatServer.resetState();

        server = new ChatServer();
        server.startServerAsync(5555);
        client = new ChatClient("localhost", 5555);

        String username = "testuser";
        String sessionId = client.login(username);
        System.out.println("Logged in with session ID: " + sessionId);

        int count = server.getConnectedUsersCount();
        assertEquals(1, count, "Expected 1 connected user");

        client.logout();
        count = server.getConnectedUsersCount();
        assertEquals(0, count, "Expected 0 connected users");

        client.shutdown();
        server.stopServer();
    }

    @Test
    public void testChatBroadcastToAllClients() throws Exception {
        System.out.println("Starting testChatBroadcastToAllClients...");

        ChatServer.resetState();

        server = new ChatServer();
        server.startServerAsync(5556);

        ChatClient client1 = new ChatClient("localhost", 5556);
        ChatClient client2 = new ChatClient("localhost", 5556);

        String user1 = "user1";
        String user2 = "user2";

        String sessionId1 = client1.login(user1);
        String sessionId2 = client2.login(user2);

        assertNotNull(sessionId1, "Session ID for client1 must not be null");
        assertNotNull(sessionId2, "Session ID for client2 must not be null");

        // Start chat streams for both clients.
        // startChatStream() will internally send a first registration message.
        client1.startChatStream();
        client2.startChatStream();

        // Now send a real chat message from client1
        String messageFromClient1 = "Hello from client1";
        client1.sendChatMessage(messageFromClient1);

        String expectedBroadcastMessage = "[" + user1 + "]: " + messageFromClient1;

        ChatMessages receivedByClient1 = client1.waitForNextMessage(3, TimeUnit.SECONDS);
        ChatMessages receivedByClient2 = client2.waitForNextMessage(3, TimeUnit.SECONDS);

        assertNotNull(receivedByClient1, "Client1 should receive a broadcast message");
        assertNotNull(receivedByClient2, "Client2 should receive a broadcast message");

        assertEquals(StatusCode.OK, receivedByClient1.getStatus(), "Client1 status should be OK");
        assertEquals(StatusCode.OK, receivedByClient2.getStatus(), "Client2 status should be OK");

        assertEquals(expectedBroadcastMessage, receivedByClient1.getMessage(),
                "Client1 should receive the correct broadcast message");
        assertEquals(expectedBroadcastMessage, receivedByClient2.getMessage(),
                "Client2 should receive the correct broadcast message");

        client1.logout();
        client2.logout();

        client1.shutdown();
        client2.shutdown();
        server.stopServer();
    }

    @Test
    public void testChatStreamClosedOnLogout() throws Exception {
        System.out.println("Starting testChatStreamClosedOnLogout...");

        ChatServer.resetState();

        server = new ChatServer();
        server.startServerAsync(5557);

        ChatClient client1 = new ChatClient("localhost", 5557);

        String user1 = "user1";
        String sessionId1 = client1.login(user1);
        assertNotNull(sessionId1, "Session ID must not be null");

        // Start chat stream; this will send a registration message internally
        client1.startChatStream();

        // Give the server some time to process registration (not strictly required, but
        // safe)
        client1.waitForNextMessage(500, TimeUnit.MILLISECONDS);
        client1.clearIncomingMessages();

        assertFalse(client1.isChatStreamCompleted(), "Chat stream should still be open before logout");

        // Now logout -> server should close the chat stream (onCompleted on client
        // side)
        client1.logout();

        // Wait a bit for onCompleted() to arrive on the client
        long deadline = System.currentTimeMillis() + 2000; // 2 seconds
        while (!client1.isChatStreamCompleted() && System.currentTimeMillis() < deadline) {
            Thread.sleep(100);
        }

        assertTrue(client1.isChatStreamCompleted(), "Chat stream should be completed after logout");

        client1.shutdown();
        server.stopServer();
    }

    @Test
    public void testListUsers() {
        System.out.println("Starting testListUsers...");

        ChatServer.resetState();

        server = new ChatServer();
        server.startServerAsync(5558);

        ChatClient client1 = new ChatClient("localhost", 5558);
        ChatClient client2 = new ChatClient("localhost", 5558);

        String user1 = "alice";
        String user2 = "bob";

        String sessionId1 = client1.login(user1);
        String sessionId2 = client2.login(user2);

        assertNotNull(sessionId1, "Session ID for client1 must not be null");
        assertNotNull(sessionId2, "Session ID for client2 must not be null");

        java.util.List<String> users = client1.listUsers();

        assertTrue(users.contains(user1), "User list should contain " + user1);
        assertTrue(users.contains(user2), "User list should contain " + user2);
        assertEquals(2, users.size(), "There should be exactly two users logged in");

        client1.logout();
        client2.logout();

        client1.shutdown();
        client2.shutdown();
        server.stopServer();
    }

}
