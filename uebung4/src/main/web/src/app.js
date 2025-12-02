// Adjust paths if your generator uses different filenames
import { ChatClient } from "../../generated/chat_grpc_web_pb";
import {
  LoginRequest,
  LogoutRequest,
  ClientMessages,
  GetUsersMessage,
  StatusCode,
  ChatStreamRequest,   // <-- add this
} from "../../generated/chat_pb";
// TODO: wire this into your DOM / UI
console.log("Client loaded:", ChatClient ? "OK" : "missing");

// URL of your gRPC-Web endpoint (behind Envoy or similar)
const client = new ChatClient("http://localhost:8080", null, null);

let sessionID = null;
let username = null;
let chatStream = null;

// DOM elements
const usernameInput = document.getElementById("username");
const loginBtn = document.getElementById("login-btn");
const logoutBtn = document.getElementById("logout-btn");
const chatLog = document.getElementById("chat-log");
const messageInput = document.getElementById("message-input");
const sendBtn = document.getElementById("send-btn");
const listUsersBtn = document.getElementById("list-users-btn");
const usersDiv = document.getElementById("users");

function logLine(line) {
  chatLog.value += line + "\n";
  chatLog.scrollTop = chatLog.scrollHeight;
}

function setLoggedInState(loggedIn) {
  usernameInput.disabled = loggedIn;
  loginBtn.disabled = loggedIn;
  logoutBtn.disabled = !loggedIn;
  sendBtn.disabled = !loggedIn;
  listUsersBtn.disabled = !loggedIn;
}

// --- Login ---

loginBtn.addEventListener("click", () => {
  const name = usernameInput.value.trim();
  if (!name) {
    alert("Please enter a username.");
    return;
  }

  const req = new LoginRequest();
  req.setUsername(name);

  client.login(req, {}, (err, resp) => {
    if (err) {
      console.error("Login error:", err);
      logLine("Login error: " + err.message);
      return;
    }

    const status = resp.getStatus();
    if (status !== StatusCode.OK) {
      logLine("Login failed (status " + status + ")");
      return;
    }

    sessionID = resp.getSessionid(); // Note: field sessionID -> getSessionid()
    username = name;
    logLine("Logged in as " + username + ", sessionID=" + sessionID);
    setLoggedInState(true);

    startChatStream();
  });
});

// --- Logout ---

logoutBtn.addEventListener("click", () => {
  if (!sessionID || !username) return;

  const req = new LogoutRequest();
  req.setUsername(username);
  req.setSessionid(sessionID);

  client.logout(req, {}, (err, resp) => {
    if (err) {
      console.error("Logout error:", err);
      logLine("Logout error: " + err.message);
      return;
    }

    const status = resp.getStatus();
    if (status !== StatusCode.OK) {
      logLine("Logout failed (status " + status + ")");
      return;
    }

    logLine("Logged out.");
    stopChatStream();
    sessionID = null;
    username = null;
    setLoggedInState(false);
  });
});

// --- Chat streaming ---
function startChatStream() {
  if (!sessionID) return;
  if (chatStream) return; // already started

  const req = new ChatStreamRequest();
  req.setSessionid(sessionID);

  // server-streaming RPC
  chatStream = client.chatStreamBrowser(req, {}); // metadata

  chatStream.on("data", (resp) => {
    const status = resp.getStatus();
    const msg = resp.getMessage();

    if (status === StatusCode.OK) {
      logLine("Server: " + msg);
    } else {
      logLine("Server (error status " + status + "): " + msg);
    }
  });

  chatStream.on("error", (err) => {
    console.error("Chat stream error:", err);
    logLine("Chat stream error: " + err.message);
    chatStream = null;
  });

  chatStream.on("end", () => {
    logLine("Chat stream closed by server.");
    chatStream = null;
  });

  logLine("Chat stream started.");
}


function stopChatStream() {
  if (chatStream) {
    // Method name varies by implementation; often 'cancel' or 'end'.
    if (typeof chatStream.cancel === "function") {
      chatStream.cancel();
    }
    chatStream = null;
  }
}

// Send a message over the stream
sendBtn.addEventListener("click", () => {
  const text = messageInput.value.trim();
  if (!text) return;
  if (!sessionID) {
    logLine("Not logged in.");
    return;
  }

  const msg = new ClientMessages();
  msg.setSessionid(sessionID);
  msg.setMessage(text);

  client.sendMessage(msg, {}, (err, resp) => {
    if (err) {
      console.error("sendMessage error:", err);
      logLine("sendMessage error: " + err.message);
      return;
    }

    const status = resp.getStatus();
    const reply = resp.getMessage();

    if (status === StatusCode.OK) {
      // You can log the user's message locally
      logLine("You: " + text);
    } else {
      logLine("sendMessage failed (status " + status + "): " + reply);
    }
  });

  messageInput.value = "";
});


// --- List users ---

listUsersBtn.addEventListener("click", () => {
  if (!sessionID) return;

  const req = new GetUsersMessage();
  req.setSessionid(sessionID);

  client.listUsers(req, {}, (err, resp) => {
    if (err) {
      console.error("listUsers error:", err);
      logLine("listUsers error: " + err.message);
      return;
    }

    const status = resp.getStatus();
    if (status !== StatusCode.OK) {
      logLine("listUsers failed (status " + status + ")");
      return;
    }

    const users = resp.getUserList(); // repeated string user
    usersDiv.textContent = users.join(", ");
    logLine("Online users: " + users.join(", "));
  });
});
