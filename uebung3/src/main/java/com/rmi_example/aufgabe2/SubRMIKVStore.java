package com.rmi_example.aufgabe2;
import java.rmi.RemoteException;
import java.util.HashMap;

import javax.naming.ldap.HasControls;
public class SubRMIKVStore implements RemoteKVStore {
    HashMap<String, String> storage = new HashMap<>();
    HashMap<String, Subscriber> subscribers = new HashMap<>();
    SubRMIKVStore(String ip, int port) throws RemoteException {
        // Create RMI Registry
        try {
            java.rmi.registry.LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            System.out.println("RMI Registry probably already exists.");
        }

    }
    @Override
    public String readRemote(String key) throws RemoteException {
        // Implementation here
        return storage.get(key);
    }

    @Override
    public void writeRemote(String key, String value) throws RemoteException {
        // Implementation here
        subscribers.get(key).updateEntry(key, value);
        storage.put(key, value);
    }

    @Override
    public void removeRemote(String key) throws RemoteException {
        // Implementation here
        subscribers.get(key).updateEntry(key, null);
        storage.remove(key);
    }
    
}