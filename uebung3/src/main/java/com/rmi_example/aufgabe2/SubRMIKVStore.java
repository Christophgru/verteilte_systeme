package com.rmi_example.aufgabe2;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SubRMIKVStore extends UnicastRemoteObject implements RemoteKVStore {

    private final Map<String, String> storage = new ConcurrentHashMap<>();
    private final Map<String, List<Subscriber>> subscribers = new ConcurrentHashMap<>();

    // Exported remote object
    protected SubRMIKVStore() throws RemoteException {
        super(0);
    }

    /* ---------------- get-or-create helper ---------------- */

    public static RemoteKVStore getOrCreate(String host, int port, String name) throws RemoteException {
        try {
            Registry reg = safeGetOrCreateRegistry(host, port);

            // Try to reuse existing binding
            try {
                return (RemoteKVStore) reg.lookup(name);
            } catch (NotBoundException e) {
                // Not bound yet: create and bind a new store
                RemoteKVStore svc = new SubRMIKVStore();
                reg.rebind(name, svc);
                System.out.println("Bound new RemoteKVStore as \"" + name + "\" on port " + port);
                return svc;
            }
        } catch (RemoteException re) {
            throw re;
        } catch (Exception e) {
            throw new RemoteException("Failed to get or create RemoteKVStore", e);
        }
    }

    private static Registry safeGetOrCreateRegistry(String host, int port) throws RemoteException {
        Registry reg;
        try {
            reg = LocateRegistry.getRegistry(host, port);
            // ping it
            reg.list();
            return reg;
        } catch (RemoteException e) {
            // No registry listening → create a new one (local JVM)
            reg = LocateRegistry.createRegistry(port);
            System.out.println("RMI registry started on port " + port);
            return reg;
        }
    }

    /* ---------------- RemoteKVStore API ---------------- */

    @Override
    public String readRemote(String key, Subscriber subscriber) throws RemoteException {
        if (subscriber != null) {
            subscribers.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
            if (!subscribers.get(key).contains(subscriber)) {
                subscribers.get(key).add(subscriber);
            }
        }
        return storage.get(key);
    }

    @Override
    public void writeRemote(String key, String value, Subscriber subscriber) throws RemoteException {
        if (subscriber != null) {
            subscribers.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
            if (!subscribers.get(key).contains(subscriber)) {
                subscribers.get(key).add(subscriber);
            }
        }
        storage.put(key, value);

        // Notify all subscribers (best-effort)
        List<Subscriber> subs = subscribers.getOrDefault(key, List.of());
        for (Subscriber s : subs) {
            try {
                s.updateEntry(key, value);
            } catch (Exception ex) {
                // drop dead subscribers
                subs.remove(s);
            }
        }
    }

    @Override
    public void removeRemote(String key) throws RemoteException {
        storage.remove(key);
        List<Subscriber> subs = subscribers.getOrDefault(key, List.of());
        for (Subscriber s : subs) {
            try {
                s.removeEntry(key);
            } catch (Exception ex) {
                // drop dead subscribers
                subs.remove(s);
            }
        }
        subscribers.remove(key);
    }
}
