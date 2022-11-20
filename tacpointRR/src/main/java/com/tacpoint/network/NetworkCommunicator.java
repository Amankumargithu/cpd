/** 
 * NetworkCommunicator - This class provides clients access to data on a
 * remote server. 
 *
 * Copyright: Tacpoint Technologies, Inc. (c), 2000.  All rights reserved.
 * @author Andy Katz
 * @author akatz@tacpoint.com
 * @version 1.0
 * Date created: 02/22/2000
 * Date modified:
 * - 02/22/2000 Initial version
 */

package com.tacpoint.network;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class provides a client with network services via a 
 * NetworkConfiguration object.  It will spawn a thread and 
 * continue to gather data until it is garbage collected or 
 * killed. 
 */
public class NetworkCommunicator implements Runnable {

    /**
     * Used to hold all NetworkConfiguration objects.
     */
    private Hashtable configs;

    /**
     * Thread responsible for executing this objects run method.
     */
    private Thread thread = null;

    /**
     * Flag used to determine when to terminate objects run method.
     */
    private boolean doRun;

    /**
     * Amount of time (in milliseconds) to sleep between network request cycles.
     */
     private long delay;

    
    /**
     * Default constructor
     */
    public NetworkCommunicator() {
       configs = new Hashtable();
       delay = 2000;
    }
    
    /**
     * Adds a NetworkConfiguration object to the hash.
     *
     * @param key Key to the configs hash. 
     * @param target The corresponding value to be inserted. 
     */
    public synchronized void addNetworkConfiguration(String key, 
                                        NetworkConfiguration target) {

        //System.out.println("About to add network configuration object.");
        //System.out.println("Key = "+key);

        NetworkConfiguration nc = (NetworkConfiguration)configs.get(key);

        if (nc==null) {
           //System.out.println("NC = null");
           configs.put(key,target);
        }
        else {
           //System.out.println("About to merge");
           nc.merge(target);
        }

        // wait until someone adds a NetworkConfiguration object before we start the run method.
        if (thread == null) {
           doRun = true;
           thread = new Thread(this);
           thread.start();
        }

        setDelay();
    }
        
    /**
     * Returns the NetworkConfiguration object from the hashtable.
     *
     * @param key The key to the requested object.
     * @return NetworkConfiguration The requested object or null.
     */
    public NetworkConfiguration getNetworkConfiguration(String key) {
        return (NetworkConfiguration)configs.get(key);
    }
    
    /**
     * Determine the smallest delay value to use based upon all entries 
     * in the NetworkConiguration hashtable. 
     */
    private  void setDelay() {
        boolean primingRead = true;
        Enumeration e = configs.elements();
        while (e.hasMoreElements()) {
           NetworkConfiguration nc = (NetworkConfiguration)e.nextElement();
           if (primingRead) {
              delay = nc.getDelay();
              primingRead = false;
              continue;
           }
           if (delay > nc.getDelay())
              delay = nc.getDelay();
        }
    }

    /**
     * Forces the thread to stop.
     */
    public synchronized void release() {
        doRun = false;
        thread = null;
    }
    private NetworkRequestExecutor nre = new NetworkRequestExecutor();
    /**
     * Used to directly extract data.
     */
    public NetworkConfiguration execute(NetworkConfiguration nc) {
       
       try {
          nre.execute(nc);
       }
       catch (Exception ex) {
          ex.printStackTrace();
          nc.setException(ex);
       }
       
       return nc;
    }

    /**
     * Overriden from Runnable inteface.
     */
    public void run() {
       
       NetworkRequestExecutor nre = new NetworkRequestExecutor();

       while(doRun) {

          Enumeration e = configs.elements();
          while (e.hasMoreElements()) {

             NetworkConfiguration nc = (NetworkConfiguration)e.nextElement();

             long currentTime = System.currentTimeMillis();

             if ((currentTime - nc.getLastExecutionTime()) > nc.getDelay()) {
                nc.setLastExecutionTime(currentTime);
                try {
                   nre.execute(nc);
                }
                catch (Exception ex) {
                   ex.printStackTrace();
                   nc.setException(ex);
                }
             }
          }
          try {
             Thread.sleep(delay);
          }
          catch (InterruptedException iex) {}
       }
    }
}

