package com.rmi_example.aufgabe1;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

public class RMIKVStore extends UnicastRemoteObject implements RemoteKVStore {

    private final ConcurrentHashMap<String, String> storage = new ConcurrentHashMap<>();

    // exporting the remote object
    protected RMIKVStore() throws RemoteException {
        super(0);
    }


    @Override
    public String readRemote(String key) throws RemoteException {
        return storage.get(key);
    }

    @Override
    public void writeRemote(String key, String value) throws RemoteException {
        storage.put(key, value);
    }

    @Override
    public void removeRemote(String key) throws RemoteException {
        storage.remove(key);
    }
}
