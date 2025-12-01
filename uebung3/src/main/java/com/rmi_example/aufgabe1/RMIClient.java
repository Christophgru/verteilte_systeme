package com.rmi_example.aufgabe1;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIClient {
    private final RemoteKVStore remote;

    public RMIClient(String host, int port) throws RemoteException {
        try {
            Registry reg = LocateRegistry.getRegistry(host, port);
            String name = RMIKVStore.class.getName();
            this.remote = (RemoteKVStore) reg.lookup(name);
        } catch (Exception e) {
            throw new RemoteException("Lookup failed", e);
        }
    }

    public String readRemote(String key) throws RemoteException {
        return remote.readRemote(key);
    }

    public void writeRemote(String key, String value) throws RemoteException {
        remote.writeRemote(key, value);
    }

    public void removeRemote(String key) throws RemoteException {
        remote.removeRemote(key);
    }
}
