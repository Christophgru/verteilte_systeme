package com.rmi_example.aufgabe2;

import junit.framework.TestCase;

public class RMIClient2Test extends TestCase {

    public void testAufgabe2() throws Exception {
        // The first client will create/bind the store if it doesn't exist.
        CachedRMIClient client1 = new CachedRMIClient("localhost", 1099);
        CachedRMIClient client2 = new CachedRMIClient("localhost", 1099);

        // Write initial value directly through the store (via get-or-create)
        RemoteKVStore store = SubRMIKVStore.getOrCreate("localhost", 1099);
        store.writeRemote("key0", "initialValue", null);

        // Both clients should see it
        assertEquals("initialValue", client1.read("key0"));
        assertEquals("initialValue", client2.read("key0"));

        // Write via client1; both should read the new value
        client1.write("key1", "value1");
        assertEquals("value1", client1.read("key1"));
        assertEquals("value1", client2.read("key1"));

        // Remove and verify
        client1.remove("key1");
        assertNull(client1.read("key1"));
        assertNull(client2.read("key1"));
    }
}
