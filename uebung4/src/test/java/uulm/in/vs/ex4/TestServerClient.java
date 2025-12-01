package uulm.in.vs.ex4;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class TestServerClient {

    ChatServer server;
    ChatClient client;

    @Test
    public void testLoginLogout() {
        System.out.println("Starting testLoginLogout...");

        // Reset static maps before this test
        ChatServer.resetState();

        server = new ChatServer();
        server.startServerAsync(5555);
        client = new ChatClient("localhost", 5555);

        String username = "testuser";
        String sessionid = client.login(username);
        System.out.println("Logged in with session ID: " + sessionid);

        int count = server.getConnectedUsersCount();
        assertEquals(1, count, "Expected 1 connected user");

        client.logout(username, sessionid);
        count = server.getConnectedUsersCount();
        assertEquals(0, count, "Expected 0 connected users");

        client.shutdown();
        server.stopServer();
    }

    @Test
    public void testChatBroadcastToAllClients() throws Exception {
        System.out.println("Starting testChatBroadcastToAllClients...");

        // Reset static maps before this test
        ChatServer.resetState();

        server = new ChatServer();
        server.startServerAsync(5556);

        ChatClient client1 = new ChatClient("localhost", 5556);
        ChatClient client2 = new ChatClient("localhost", 5556);

        // Login both clients
        String user1 = "user1";
        String user2 = "user2";
        String sessionId1 = client1.login(user1);
        String sessionId2 = client2.login(user2);

        assertNotNull(sessionId1, "Session ID for client1 must not be null");
        assertNotNull(sessionId2, "Session ID for client2 must not be null");

        // Start chat streams for both clients
        client1.startChatStream(sessionId1);
        client2.startChatStream(sessionId2);

        // First message from each client is used by the server to register them.
        client1.sendChatMessage("register-client1");
        client2.sendChatMessage("register-client2");

        // Give the server a moment to process registration messages
        client1.waitForNextMessage(500, TimeUnit.MILLISECONDS);
        client2.waitForNextMessage(500, TimeUnit.MILLISECONDS);

        // Clear any messages from registration phase
        client1.clearIncomingMessages();
        client2.clearIncomingMessages();

        // Now send a real chat message from client1 – this should be broadcast to all
        String messageFromClient1 = "Hello from client1";
        client1.sendChatMessage(messageFromClient1);

        String expectedBroadcastMessage = "[" + sessionId1 + "]: " + messageFromClient1;

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

        // Cleanup
        client1.stopChatStream();
        client2.stopChatStream();

        client1.logout(user1, sessionId1);
        client2.logout(user2, sessionId2);

        client1.shutdown();
        client2.shutdown();
        server.stopServer();
    }
}