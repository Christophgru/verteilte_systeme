package com.rmi_example.aufgabe1;

import junit.framework.TestCase;
import java.rmi.RemoteException;
//mvn -Dtest=RMIClient1Test test
public class RMIClient1Test extends TestCase {

    /**
     * Rigourous Test :-)
     */
    public void testAufgabe1()
    {
         RMIKVStore store=null;
        try {
            store=new RMIKVStore("localhost",1099);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(store==null){
        System.out.println("RMIKVStore is null");
        return;
        }
        System.out.println("RMIKVStore created successfully");
        //create client and connect to server
        RMIClient client=null;
        try {
            client = new RMIClient("localhost", 1099);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        
        if(client==null){
            System.out.println("RMIClient is null");
            return;
        }
        System.out.println("RMIClient created successfully");

        //test write
        try {
            client.writeRemote("key1", "value1");
            System.out.println("Wrote key1:value1");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //test read
        try {
            String value = client.readRemote("key1");
            assertEquals("value1", value);
            System.out.println("Read key1:"+value);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //test remove
        try {
            client.removeRemote("key1");
            System.out.println("Removed key1");
        } catch (RemoteException e) {
            e.printStackTrace();
        }   
        //read again
        try {
            String value = client.readRemote("key1");
            assertEquals(null, value);
            System.out.println("Read key1 after removal:"+value);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        //write over store object
        try {
            //I expect this to result in the client reading "value2" afterwards
            store.writeRemote("key2", "value2");
            System.out.println("Wrote key2:value2 over store object");
            String result = client.readRemote("key2");
            //right now it reads null
            assertEquals("value2", result);
            System.out.println("Read key2 after writing over store object:"+result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
