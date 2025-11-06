package com.rmi_example.aufgabe2;

public interface Subscriber extends java.rmi.Remote {
    void updateEntry(String key, String value);
    void removeEntry(String key);
}
