package com.rmi_example.aufgabe2;
import java.rmi.RemoteException;
public interface SubscribeKVStore extends RemoteKVStore {

    void subscribe(String key, Subscriber callback) throws RemoteException;
    void unsubscribe(String key, Subscriber callback) throws RemoteException;
    
}