package uulm.in.vs.ex4;

import org.junit.jupiter.api.Test;

public class TestServerClient {
    ChatServer server;
    ChatClient client;

    @Test
    public void testLoginLogout() {
        System.out.println("Starting testLoginLogout...");
        server = new ChatServer();
        server.startServerAsync(5555);
        client = new ChatClient("localhost", 5555);

        String username = "testuser";
        String sessionid = client.login(username);
        System.out.println("Logged in with session ID: " + sessionid);

        int count = server.getConnectedUsersCount();
        assert (count == 1) : "Expected 1 connected user, got " + count;

        client.logout(username, sessionid);
        count = server.getConnectedUsersCount();
        assert (count == 0) : "Expected 0 connected users, got " + count;

        server.stopServer();
    }
}
