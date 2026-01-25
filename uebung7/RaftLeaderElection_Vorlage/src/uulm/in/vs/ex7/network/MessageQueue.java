package uulm.in.vs.ex7.network;

import uulm.in.vs.ex7.messages.NetworkMessage;

import java.util.PriorityQueue;

public class MessageQueue {
    private final PriorityQueue<NetworkMessage> queue = new PriorityQueue<>();

    public synchronized void add(NetworkMessage msg) {
        if(queue.isEmpty()) {
            queue.add(msg);
            notify();
        } else {
            queue.add(msg);
            if(queue.peek().equals(msg))
                notify();
        }
    }

    public synchronized NetworkMessage take() throws InterruptedException {
        while(queue.isEmpty()) {
            wait();
            // Prevent spurious wakeup
            if(!queue.isEmpty())
                break;
        }

        while(true) {
            NetworkMessage message = queue.peek();
            long offset = message.getDeliveryTime() - System.currentTimeMillis();
            if (offset <= 0) {
                return queue.poll();
            } else {
                wait(offset);
            }
        }
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

}
