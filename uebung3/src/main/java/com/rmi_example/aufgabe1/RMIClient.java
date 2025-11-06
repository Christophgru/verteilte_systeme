package com.rmi_example.aufgabe1;
import java.rmi.RemoteException;
public class RMIClient implements RemoteKVStore {
    RMIKVStore store = null;
    RMIClient(String ip, int port) throws RemoteException {
        // get RMI Registry
        try {
            store = new RMIKVStore(ip, port);
        } catch (RemoteException e) {
            e.printStackTrace();    
        }

    }
    @Override
    public String readRemote(String key) throws RemoteException {
        return store.readRemote(key);
    }

    @Override
    public void writeRemote(String key, String value) throws RemoteException {
        store.writeRemote(key, value);
    }

    @Override
    public void removeRemote(String key) throws RemoteException {
        store.removeRemote(key);
    }
    
}