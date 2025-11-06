package com.rmi_example.aufgabe1;

import junit.framework.TestCase;
//mvn test
//or for just this file use
//mvn -Dtest=RMIClient1Test test

public class RMIClient1Test extends TestCase {

    public void testAufgabe1() throws Exception {
        // ensure registry + binding exist
        RemoteKVStore store = StoreBootstrap.getOrCreate("localhost", 1099);
        assertNotNull(store);
        System.out.println("RMIKVStore created/bound successfully");

        // client looks up the same remote object
        RMIClient client = new RMIClient("localhost", 1099);
        assertNotNull(client);
        System.out.println("RMIClient created successfully");

        // write via client
        client.writeRemote("key1", "value1");
        assertEquals("value1", client.readRemote("key1"));
        client.removeRemote("key1");
        assertNull(client.readRemote("key1"));

        // write via store stub (same remote object) → read via client
        store.writeRemote("key2", "value2");
        String result = client.readRemote("key2");
        assertEquals("value2", result);
    }
}
