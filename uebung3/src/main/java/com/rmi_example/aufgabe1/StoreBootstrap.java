package com.rmi_example.aufgabe1;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

final class StoreBootstrap {
    private StoreBootstrap() {}

    static RemoteKVStore getOrCreate(String host, int port) throws RemoteException {
        Registry reg;
        try {
            reg = LocateRegistry.getRegistry(host, port);
            reg.list(); // ping
        } catch (RemoteException e) {
            reg = LocateRegistry.createRegistry(port);
            System.out.println("RMI registry started on port " + port);
        }
        String name = RMIKVStore.class.getName();
        try {
            return (RemoteKVStore) reg.lookup(name);
        } catch (NotBoundException e) {
            RemoteKVStore store = new RMIKVStore();
            reg.rebind(name, store);
            System.out.println("Bound new RemoteKVStore as \"" + name + "\"");
            return store;
        }
    }
}
