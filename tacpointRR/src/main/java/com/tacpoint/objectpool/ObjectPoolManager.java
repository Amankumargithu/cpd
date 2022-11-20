
package com.tacpoint.objectpool;

import java.io.*;
import java.util.*;
import java.util.Date;

import com.tacpoint.util.*;

/**
 * This class is a Singleton that provides access to an Object. 
 * A client gets access to the single instance through the static 
 * getInstance() method and can then check-out and check-in Objects 
 * from a pool.
 */
public class ObjectPoolManager {

    static private ObjectPoolManager instance;       
    static private int clients;
    private Hashtable pools = new Hashtable();

    /**
     * Returns the single instance, creating one if it's the
     * first time this method is called.
     *
     * @return ObjectPoolManager The single instance.
     */
    static synchronized public ObjectPoolManager getInstance() {
        if (instance == null) {
            instance = new ObjectPoolManager();
        }
        clients++;
        return instance;
    }
    
    /**
     * A private constructor 
     */
    private ObjectPoolManager() {
        init();
    }
    
    /**
     * Returns a Object to the named pool.
     *
     * @param name The pool name as defined in the properties file
     * @param Object The Object
     */
    public void freeObject(String name, Object object) {
        ObjectPool pool = (ObjectPool) pools.get(name);
        if (pool != null) {
            pool.freeObject(object);
        }
    }
        
    /**
     * Returns a live object. If no object is available, and the max
     * number of objects has not been reached, a new object is
     * created.
     *
     * @param name The pool name as defined in the properties file
     * @return Object The object or null
     */
    public Object getObject(String name) {
        ObjectPool pool = (ObjectPool) pools.get(name);
        if (pool != null) {
            return pool.getObject();
        }
        return null;
    }
    
    /**
     * Returns a new Object. If no one is available, and the max
     * number of Objects has not been reached, a new Object is
     * created. If the max number has been reached, waits until one
     * is available or the specified time has elapsed.
     *
     * @param name The pool name as defined in the properties file
     * @param time The number of milliseconds to wait
     * @return Object The object or null
     */
    public Object getObject(String name, long time) {
        ObjectPool pool = (ObjectPool) pools.get(name);
        if (pool != null) {
            return pool.getObject(time);
        }
        return null;
    }
    
    /**
     * No op method
     */
    public synchronized void release() {
        // Wait until called by the last client
        if (--clients != 0) {
            return;
        }
    }
    
    /**
     * Creates instances of ObjectPool based on the properties.
     * A ObjectPool can be defined with the following properties:
     * <PRE>
     * &lt;poolname&gt;.objectClassName    The classname of the concrete Object
     * &lt;poolname&gt;.maxobjects    The maximal number of Objects (optional)
     * </PRE>
     *
     * @param props The objects pool properties
     */
    private void createPools(Properties props) {
        Enumeration propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String name = (String) propNames.nextElement();
            if (name.endsWith(".objectClassName")) {
                String poolName = name.substring(0, name.lastIndexOf("."));
                String objectClassName = props.getProperty(poolName + ".objectClassName");
                String maxobjects = props.getProperty(poolName + ".maxobjects", "0");
                int max;
                try {
                    max = Integer.valueOf(maxobjects).intValue();
                }
                catch (NumberFormatException e) {
                    Logger.log("ObjectPoolManager.createPools - Invalid maxobjects value " + maxobjects+ " for " + poolName);
                    max = 0;
                }
                ObjectPool pool = new ObjectPool(poolName, objectClassName, max);
                pools.put(poolName, pool);
                Logger.log("ObjectPoolManager.createPools - pool name = " + poolName);
                Logger.log("ObjectPoolManager.createPools - object class name = " + objectClassName);
                Logger.log("ObjectPoolManager.createPools - max objects = " + maxobjects);
            }
        }
    }
    
    /**
     * Loads properties and initializes the instance with its values.
     */
    private void init() {
        InputStream is = getClass().getResourceAsStream("/objectpool_manager.properties");
        Properties dbProps = new Properties();
        try {
            dbProps.load(is);
        }
        catch (Exception e) {
            Logger.log("ObjectPoolManager.init - Unable to read the properties file.  Make sure "+
                       "objectpool_manager.properties is in the CLASSPATH",e);
            return;
        }

        createPools(dbProps);

    }
    
    /**
     * This inner class represents a object pool. It creates new
     * objects on demand, up to a max number if specified.
     */
    class ObjectPool {
        private int checkedOut;
        private Vector freeObjects = new Vector();
        private int maxObjects;
        private String name;
        private String objectClassName;
        
        /**
         * Creates new object pool.
         *
         * @param name The pool name
         * @param objectClassName The class name to be instantiated 
         * @param maxObjects The maximal number of objects, or 0 for no limit
         */
        public ObjectPool(String name, String objectClassName, int maxObjects) {
            this.name = name;
            this.objectClassName = objectClassName;
            this.maxObjects = maxObjects;
        }

        
        /**
         * A helper method to allow for object clean-up
         */
        public Vector getFreeObjects() {
           return freeObjects;
        }

        /**
         * Checks in a object to the pool. Notify other Threads that
         * may be waiting for a object.
         *
         * @param object The object to check in
         */
        public synchronized void freeObject(Object object) {
            // Put the object at the end of the Vector
            freeObjects.addElement(object);
            checkedOut--;
            notifyAll();
        }
        
        /**
         * Checks out a object from the pool. If no free object
         * is available, a new object is created unless the max
         * number of objects has been reached. 
         */
        public synchronized Object getObject() {
            Object object = null;
            if (freeObjects.size() > 0) {
                // Pick the first Object in the Vector
                // to get round-robin usage
                object = (Object)freeObjects.firstElement();
                freeObjects.removeElementAt(0);
            }
            else if (maxObjects == 0 || checkedOut < maxObjects) {
                Logger.log("ObjectPool.getObject - adding new object ["+
                            objectClassName+
                           "] to pool.  Current pool count = "+
                            (checkedOut+1));
                object = newObject();
            }
            if (object != null) {
                checkedOut++;
            }
            return object;
        }
        
        /**
         * Checks out a object from the pool. If no free object
         * is available, a new object is created unless the max
         * number of objects has been reached. 
         * <P>
         * If no object is available and the max number has been 
         * reached, this method waits the specified time for one to be
         * checked in.
         *
         * @param timeout The timeout value in milliseconds
         */
        public synchronized Object getObject(long timeout) {
            long startTime = new Date().getTime();
            Object object;
            while ((object = getObject()) == null) {
                try {
                    wait(timeout);
                }
                catch (InterruptedException e) {}
                if ((new Date().getTime() - startTime) >= timeout) {
                    // Timeout has expired
                    return null;
                }
            }
            return object;
        }
        
        /**
         * No-op method
         */
        public synchronized void release() {}
        
        /**
         * Creates a new object.
         */
        private Object newObject() {
            Object object = null;
            try {
                object = (Object)Class.forName(objectClassName).newInstance();
            }
            catch (Exception e) {
               Logger.log("ObjectPool.newObject - Unable to instantiate Object ["+objectClassName+"]",e);
            }
            return object;
        }
    }
}

