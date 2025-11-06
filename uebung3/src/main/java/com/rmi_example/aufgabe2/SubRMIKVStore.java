package com.rmi_example.aufgabe2;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;

public class SubRMIKVStore implements RemoteKVStore {
    HashMap<String, String> storage = new HashMap<>();
    HashMap<String, List<Subscriber>> subscribers = new HashMap<>();
    SubRMIKVStore(String ip, int port) throws RemoteException {
        // Create RMI Registry
       // Start registry if not already running
        try {
            LocateRegistry.createRegistry(port);
            System.out.println("RMI registry started on " + port);
        } catch (RemoteException e) {
            System.out.println("RMI Registry probably already exists.");
        }

          // Export this server object and bind to registry
        RemoteKVStore stub = (RemoteKVStore) UnicastRemoteObject.exportObject(this, 0);
        Registry reg = LocateRegistry.getRegistry(ip, port);
        reg.rebind("KVStore", stub);
        System.out.println("KVStore bound");

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
        storage.put(key, value);

        for (Subscriber subscriber_n : subscribers.get(key)) {
            if( subscriber_n != subscriber)
            subscriber_n.updateEntry(key, value);
        }
    }

    @Override
    public void removeRemote(String key) throws RemoteException {
        // Implementation here
        List<Subscriber> subs = subscribers.get(key);
        if (subs != null) {
            for (Subscriber s : subs) {
                try { s.updateEntry(key, null); } catch (Exception ignored) {}
            }
        }
        subscribers.remove(key);
        storage.remove(key);
    }
    
}