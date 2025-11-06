package com.rmi_example.aufgabe2;
import java.rmi.Remote;
import java.rmi.RemoteException;
public interface RemoteKVStore extends Remote {


    String readRemote(String key, Subscriber subscriber) throws RemoteException;
    void writeRemote(String key,String value,Subscriber subscriber) throws RemoteException;
    void removeRemote(String key) throws RemoteException;

    
}