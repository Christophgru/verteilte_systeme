package com.rmi_example.aufgabe2;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
public class CachedRMIClient implements Subscriber  {
    SubRMIKVStore store = null;
    HashMap<String, String> cache = new HashMap<>();
    CachedRMIClient(String ip, int port) throws RemoteException {
        // get RMI Registry
       try {
            // export 'this' so server can call back
            UnicastRemoteObject.exportObject(this, 0);

            Registry registry = LocateRegistry.getRegistry(ip, port);
            this.store = (SubRMIKVStore) registry.lookup("KVStore");
        } catch (Exception e) {
            throw new RemoteException("Failed to connect/lookup KVStore", e);
        }

    }
    @Override
    public void updateEntry(String key, String value) {
        if(value == null){
            cache.remove(key);
            return;
        }
        cache.put(key, value);
    }
    @Override
    public void removeEntry(String key) {
        cache.remove(key);
    }
    public String read(String key) throws RemoteException {
        if (cache.containsKey(key)) {
            return cache.get(key);
        } else {
            String value = store.readRemote(key, this);
            cache.put(key, value);
            return value;
        }
    }
    public void write(String key, String value) throws RemoteException {
        cache.put(key, value);
        store.writeRemote(key, value, this);
    }
    public void remove(String key) throws RemoteException {
        cache.remove(key);
        store.removeRemote(key);
    }
    
}