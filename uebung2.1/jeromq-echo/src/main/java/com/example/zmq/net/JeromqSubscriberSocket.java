package com.example.zmq.net;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
public class JeromqSubscriberSocket {

    private ZMQ.Socket socket;
    
    public JeromqSubscriberSocket(ZContext ctx, String endpoint) {
        this.socket = ctx.createSocket(SocketType.SUB);
        this.socket.connect(endpoint);
        this.socket.subscribe("".getBytes(ZMQ.CHARSET));
    }

    public String receiveMessage() {
        byte[] reply = this.socket.recv(0);
        return new String(reply, ZMQ.CHARSET);
    }

    public void close() {
        this.socket.close();
    }
}
