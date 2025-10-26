package com.example.zmq.net;


    
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

/**
 * Tiny wrapper around a JeroMQ REQ socket.
 * Connects on construction. Not thread-safe (REQ must strictly send→recv).
 */
public final class JeromqPushSocket implements AutoCloseable {
    private final ZMQ.Socket pushSocket;

    /**
     * Connect and configure a PUSH socket.
     *
     * @param endpoint       tcp://host:port to connect to (e.g., tcp://127.0.0.1:5555)
     * @param recvTimeoutMs  how long to wait for replies (0 = block forever; >0 = ms)
     */
    public JeromqPushSocket(ZContext ctx, String endpoint, boolean bind) {
        this.pushSocket = ctx.createSocket(SocketType.PUSH);
        // quick shutdowns; don’t hang on close
        if(bind) {
            this.pushSocket.bind( endpoint);
            System.out.println("[JeromqPushSocket] Bound to " + endpoint);
        } else {
            this.pushSocket.connect( endpoint);
        }
        System.out.println("[JeromqPushSocket] Connected to " + endpoint);
    }

    /** Send a UTF-8 string (one-part message). */
    public void push(String msg) {
        this.pushSocket.send(msg);
    }

    /** Receive a UTF-8 string or null on timeout/interruption. */
    public String recv() {
        return this.pushSocket.recvStr();
    }


    @Override
    public void close() {
        if (pushSocket != null) this.pushSocket.close();
    }
}


