
package com.tacpoint.task;

import java.io.*;
import java.util.*;
import java.util.Date;

import com.tacpoint.util.*;

/**
 * This class is a Singleton that provides access to a concrete
 * instance of a Task interface implementation.  A client gets 
 * access to the single instance through the static getInstance() 
 * method and can then check-out and check-in Tasks from a pool.
 */
public class TaskManager {

    static private TaskManager instance;       
    static private int clients;
    private Hashtable pools = new Hashtable();

    /**
     * Returns the single instance, creating one if it's the
     * first time this method is called.
     *
     * @return TaskManager The single instance.
     */
    static synchronized public TaskManager getInstance() {
        if (instance == null) {
            instance = new TaskManager();
        }
        clients++;
        return instance;
    }
    
    /**
     * A private constructor 
     */
    private TaskManager() {
        init();
    }
    
    /**
     * Returns a task to the named pool.
     *
     * @param name The pool name as defined in the properties file
     * @param task The Task
     */
    public void freeTask(String name, Task task) {
        TaskPool pool = (TaskPool) pools.get(name);
        if (pool != null) {
            pool.freeTask(task);
        }
    }
        
    /**
     * Returns a live task. If no task is available, and the max
     * number of tasks has not been reached, a new task is
     * created.
     *
     * @param name The pool name as defined in the properties file
     * @return Task The task or null
     */
    public Task getTask(String name) {
        TaskPool pool = (TaskPool) pools.get(name);
        if (pool != null) {
            return pool.getTask();
        }
        return null;
    }
    
    /**
     * Returns a new Task. If no one is available, and the max
     * number of Tasks has not been reached, a new Task is
     * created. If the max number has been reached, waits until one
     * is available or the specified time has elapsed.
     *
     * @param name The pool name as defined in the properties file
     * @param time The number of milliseconds to wait
     * @return Task The task or null
     */
    public Task getTask(String name, long time) {
        TaskPool pool = (TaskPool) pools.get(name);
        if (pool != null) {
            return pool.getTask(time);
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
     * Creates instances of TaskPool based on the properties.
     * A TaskPool can be defined with the following properties:
     * <PRE>
     * &lt;poolname&gt;.taskClassName    The classname of the concrete Task
     * &lt;poolname&gt;.maxtasks    The maximal number of Tasks (optional)
     * </PRE>
     *
     * @param props The task pool properties
     */
    private void createPools(Properties props) {
        Enumeration propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String name = (String) propNames.nextElement();
            if (name.endsWith(".taskClassName")) {
                String poolName = name.substring(0, name.lastIndexOf("."));
                String taskClassName = props.getProperty(poolName + ".taskClassName");
                String maxtasks = props.getProperty(poolName + ".maxtasks", "0");
                int max;
                try {
                    max = Integer.valueOf(maxtasks).intValue();
                }
                catch (NumberFormatException e) {
                    Logger.log("TaskManager.createPools - Invalid maxtasks value " + maxtasks + " for " + poolName);
                    max = 0;
                }
                TaskPool pool = new TaskPool(poolName, taskClassName, max);
                pools.put(poolName, pool);
                Logger.log("TaskManager.createPools - pool name = " + poolName);
                Logger.log("TaskManager.createPools - task class name = " + taskClassName);
                Logger.log("TaskManager.createPools - max tasks = " + maxtasks);
            }
        }
    }
    
    /**
     * Loads properties and initializes the instance with its values.
     */
    private void init() {
        InputStream is = getClass().getResourceAsStream("/task_manager.properties");
        Properties dbProps = new Properties();
        try {
            dbProps.load(is);
        }
        catch (Exception e) {
            Logger.log("TaskManager.init - Unable to read the properties file.  Make sure "+
                       "task_manager.properties is in the CLASSPATH",e);
            return;
        }

        createPools(dbProps);

    }
    
    /**
     * This inner class represents a task pool. It creates new
     * tasks on demand, up to a max number if specified.
     */
    class TaskPool {
        private int checkedOut;
        private Vector freeTasks = new Vector();
        private int maxTasks;
        private String name;
        private String taskClassName;
        
        /**
         * Creates new task pool.
         *
         * @param name The pool name
         * @param taskClassName The class name to be instantiated 
         * @param maxTasks The maximal number of tasks, or 0 for no limit
         */
        public TaskPool(String name, String taskClassName, int maxTasks) {
            this.name = name;
            this.taskClassName = taskClassName;
            this.maxTasks = maxTasks;
        }

        
        /**
         * A helper method to allow for task clean-up
         */
        public Vector getFreeTasks() {
           return freeTasks;
        }

        /**
         * Checks in a task to the pool. Notify other Threads that
         * may be waiting for a task.
         *
         * @param task The task to check in
         */
        public synchronized void freeTask(Task task) {
            // Put the task at the end of the Vector
            freeTasks.addElement(task);
            checkedOut--;
            notifyAll();
        }
        
        /**
         * Checks out a task from the pool. If no free task
         * is available, a new task is created unless the max
         * number of tasks has been reached. 
         */
        public synchronized Task getTask() {
            Task task = null;
            if (freeTasks.size() > 0) {
                // Pick the first Task in the Vector
                // to get round-robin usage
                task = (Task)freeTasks.firstElement();
                freeTasks.removeElementAt(0);
            }
            else if (maxTasks == 0 || checkedOut < maxTasks) {
                task = newTask();
            }
            if (task != null) {
                checkedOut++;
            }
            return task;
        }
        
        /**
         * Checks out a task from the pool. If no free task
         * is available, a new task is created unless the max
         * number of tasks has been reached. 
         * <P>
         * If no task is available and the max number has been 
         * reached, this method waits the specified time for one to be
         * checked in.
         *
         * @param timeout The timeout value in milliseconds
         */
        public synchronized Task getTask(long timeout) {
            long startTime = new Date().getTime();
            Task task;
            while ((task = getTask()) == null) {
                try {
                    wait(timeout);
                }
                catch (InterruptedException e) {}
                if ((new Date().getTime() - startTime) >= timeout) {
                    // Timeout has expired
                    return null;
                }
            }
            return task;
        }
        
        /**
         * No-op method
         */
        public synchronized void release() {}
        
        /**
         * Creates a new task.
         */
        private Task newTask() {
            Task task = null;
            try {
                task = (Task)Class.forName(taskClassName).newInstance();
            }
            catch (Exception e) {
               Logger.log("TaskPool.newTask - Unable to instantiate Task ["+taskClassName+"]",e);
            }
            return task;
        }
    }
}

