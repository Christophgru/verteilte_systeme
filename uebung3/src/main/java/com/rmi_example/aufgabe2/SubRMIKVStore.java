package com.rmi_example.aufgabe2;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

public class SubRMIKVStore implements RemoteKVStore {
    HashMap<String, String> storage = new HashMap<>();
    HashMap<String, List<Subscriber>> subscribers = new HashMap<>();
    SubRMIKVStore(String ip, int port) throws RemoteException {
        // Create RMI Registry
        try {
            java.rmi.registry.LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            System.out.println("RMI Registry probably already exists.");
        }

    }
    @Override
    public String readRemote(String key, Subscriber subscriber) throws RemoteException {
        // Implementation here
        subscribers.putIfAbsent(key, new java.util.ArrayList<>());
        if (subscriber != null && !subscribers.get(key).contains(subscriber)) {
            subscribers.get(key).add(subscriber);
        }
        return storage.get(key);
    }

    @Override
    public void writeRemote(String key, String value, Subscriber subscriber) throws RemoteException {
        // Implementation here
        subscribers.putIfAbsent(key, new java.util.ArrayList<>());
        if (subscriber != null && !subscribers.get(key).contains(subscriber)) {
            subscribers.get(key).add(subscriber);
        }
        for (Subscriber subscriber_n : subscribers.get(key)) {
            if( subscriber_n != subscriber)
            subscriber_n.updateEntry(key, value);
        }
        storage.put(key, value);
    }

    @Override
    public void removeRemote(String key) throws RemoteException {
        // Implementation here
        for (Subscriber subscriber : subscribers.get(key)) {
            subscriber.updateEntry(key, null);
        }
        subscribers.remove(key);
        storage.remove(key);
    }
    
}