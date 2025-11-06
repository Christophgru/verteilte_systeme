package com.rmi_example.aufgabe1;
import java.rmi.Remote;
import java.rmi.RemoteException;
public interface RemoteKVStore extends Remote {


    String readRemote(String key) throws RemoteException;
    void writeRemote(String key,String value) throws RemoteException;
    void removeRemote(String key) throws RemoteException;

    
}