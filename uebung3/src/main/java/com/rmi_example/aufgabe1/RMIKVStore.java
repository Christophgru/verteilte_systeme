package com.rmi_example.aufgabe1;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

public class RMIKVStore implements RemoteKVStore {
    // 🔧 make storage shared across all instances
    private static final ConcurrentHashMap<String, String> STORAGE = new ConcurrentHashMap<>();

    RMIKVStore(String ip, int port) throws RemoteException {
        try {
            // be polite: reuse existing registry if present
            java.rmi.registry.LocateRegistry.getRegistry(port).list(); // ping
        } catch (RemoteException e) {
            // none running -> create one
            java.rmi.registry.LocateRegistry.createRegistry(port);
        }
    }

    @Override
    public String readRemote(String key) throws RemoteException {
        return STORAGE.get(key);
    }

    @Override
    public void writeRemote(String key, String value) throws RemoteException {
        STORAGE.put(key, value);
    }

    @Override
    public void removeRemote(String key) throws RemoteException {
        STORAGE.remove(key);
    }
}
