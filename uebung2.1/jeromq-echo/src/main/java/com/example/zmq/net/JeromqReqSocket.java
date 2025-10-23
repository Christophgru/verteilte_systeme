package com.example.zmq.net;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

/**
 * Tiny wrapper around a JeroMQ REQ socket.
 * Connects on construction. Not thread-safe (REQ must strictly send→recv).
 */
public final class JeromqReqSocket implements AutoCloseable {
    private final ZContext ctx;
    private final ZMQ.Socket req;

    /**
     * Connect and configure a REQ socket.
     *
     * @param endpoint       tcp://host:port to connect to (e.g., tcp://127.0.0.1:5555)
     * @param recvTimeoutMs  how long to wait for replies (0 = block forever; >0 = ms)
     */
    public JeromqReqSocket(String endpoint, int recvTimeoutMs) {
        this.ctx = new ZContext();
        this.req = ctx.createSocket(SocketType.REQ);
        // quick shutdowns; don’t hang on close
        this.req.setLinger(0);
        this.req.setReceiveTimeOut(recvTimeoutMs);

        System.out.println("[JeromqReqSocket] Connecting to " + endpoint);
        this.req.connect(endpoint);
    }

    /** Send a UTF-8 string (one-part message). */
    public void send(String msg) {
        req.send(msg);
    }

    /** Receive a UTF-8 string or null on timeout/interruption. */
    public String recv() {
        return req.recvStr();
    }

    /** Convenience: REQ pattern request/response in one call. */
    public String request(String msg) {
        send(msg);
        return recv();
    }

    @Override
    public void close() {
        try {
            if (req != null) req.close();
        } finally {
            if (ctx != null) ctx.close();
        }
    }
}
