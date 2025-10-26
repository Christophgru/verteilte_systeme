package com.example.zmq.net;


    
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

/**
 * Tiny wrapper around a JeroMQ REQ socket.
 * Connects on construction. Not thread-safe (REQ must strictly send→recv).
 */
public final class JeromqPullSocket implements AutoCloseable {
    private final ZMQ.Socket pullSocket;

    /**
     * Connect and configure a PUSH socket.
     *
     * @param endpoint       tcp://host:port to connect to (e.g., tcp://127.0.0.1:5555)
     * @param recvTimeoutMs  how long to wait for replies (0 = block forever; >0 = ms)
     */
    public JeromqPullSocket(ZContext ctx, String endpoint, boolean bind) {
        this.pullSocket = ctx.createSocket(SocketType.PULL);
        // quick shutdowns; don’t hang on close
        if(bind) {
            this.pullSocket.bind( endpoint);
            System.out.println("[JeromqPullSocket] Bound to " + endpoint);
        } else {
            this.pullSocket.connect( endpoint);
        }
        System.out.println("[JeromqPullSocket] Connected to " + endpoint);
    }


    /** Receive a UTF-8 string or null on timeout/interruption. */
    public String recv() {
        return this.pullSocket.recvStr();
    }
    /** Set receive timeout in milliseconds (0 = block forever; >0 = ms). */
    public void setReceiveTimeOut(int timeoutMs) {
        this.pullSocket.setReceiveTimeOut(timeoutMs);
    }


    @Override
    public void close() {
        if (pullSocket != null) pullSocket.close();
    }
}


