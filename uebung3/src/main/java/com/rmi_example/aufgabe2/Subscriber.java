package com.rmi_example.aufgabe2;

public interface Subscriber {
    void updateEntry(String key, String value);
    void removeEntry(String key);
}
