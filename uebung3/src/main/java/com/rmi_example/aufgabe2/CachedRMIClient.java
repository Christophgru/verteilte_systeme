package com.rmi_example.aufgabe2;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachedRMIClient implements Subscriber {
    private final RemoteKVStore store;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public CachedRMIClient(String host, int port, String name) throws Exception {
        // Ensure the server exists (lookup or create + bind)
        this.store = SubRMIKVStore.getOrCreate(host, port, name);

        // Export this client so server can callback updateEntry/removeEntry
        UnicastRemoteObject.exportObject(this, 0);
    }

    /* ---------- Subscriber callbacks (from server) ---------- */
    @Override
    public void updateEntry(String key, String value)  {
        if (value == null) {
            cache.remove(key);
        } else {
            cache.put(key, value);
        }
    }

    @Override
    public void removeEntry(String key)  {
        cache.remove(key);
    }

    /* ---------- Client API ---------- */
    public String read(String key) throws RemoteException {
        String cached = cache.get(key);
        if (cached != null) return cached;

        String value = store.readRemote(key, this);
        if (value != null) cache.put(key, value);
        return value;
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
