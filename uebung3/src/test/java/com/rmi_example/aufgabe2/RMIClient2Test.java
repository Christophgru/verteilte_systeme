package com.rmi_example.aufgabe2;

import junit.framework.TestCase;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

//mvn -Dtest=RMIClient2Test test
public class RMIClient2Test extends TestCase {

    public void testAufgabe2() {
        SubRMIKVStore store=null;
        try {
            store=new SubRMIKVStore("localhost",1099);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(store==null){
        System.out.println("RMIKVStore is null");
        assertTrue(false);
        return;
        }
        System.out.println("RMIKVStore created successfully");
        //create client and connect to server
        CachedRMIClient client1=null;
        try {
            client1 = new CachedRMIClient("localhost", 1099);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        
        if(client1==null){
            System.out.println("RMIClient1 is null");
            return;
        }
        System.out.println("RMIClient1 created successfully");
        CachedRMIClient client2=null;
        try {
            client2 = new CachedRMIClient("localhost", 1099);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        if(client2==null){
            System.out.println("RMIClient2 is null");
            return;
        }
        System.out.println("RMIClient2 created successfully");
        //write server
        try {
            store.writeRemote("key0", "initialValue", null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //test read initial value from both clients
        try {
            String value = client1.read("key0");
            assertEquals("initialValue", value);
            System.out.println("Client1 Read key0:"+value);
            String value2 = client2.read("key0");
            assertEquals("initialValue", value2);
            System.out.println("Client2 Read key0:"+value2);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        //test write
        try {
            client1.write("key1", "value1");
            System.out.println("Wrote key1:value1");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //test read
        try {
            //expect "value1" for both clients
            String value = client1.read("key1");
            assertEquals("value1", value);
            System.out.println("Client1 Read key1:"+value);
            String value2 = client2.read("key1");
            assertNotNull(value2);
            assertEquals("value1", value2);
            System.out.println("Client2 Read key1:"+value2);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //test remove
        try {
            client1.remove("key1");
            System.out.println("Removed key1");
        } catch (RemoteException e) {
            e.printStackTrace();
        }   
        //read again
        try {
            String value = client1.read("key1");
            assertEquals(null, value);
            System.out.println("Read key1 after removal:"+value);
            String value2 = client2.read("key1");
            assertEquals(null, value2);
            System.out.println("Client2 Read key1 after removal:"+value2);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
