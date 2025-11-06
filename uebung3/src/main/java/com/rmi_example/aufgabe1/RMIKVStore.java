package com.rmi_example.aufgabe1;
import java.rmi.RemoteException;
import java.util.HashMap;
public class RMIKVStore implements RemoteKVStore {
    HashMap<String, String> storage = new HashMap<>();
    RMIKVStore(String ip, int port) throws RemoteException {
        // Create RMI Registry
        try {
            java.rmi.registry.LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            System.out.println(e.getMessage());
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
        storage.put(key, value);
    }

    @Override
    public void removeRemote(String key) throws RemoteException {
        // Implementation here
        storage.remove(key);
    }
    
}