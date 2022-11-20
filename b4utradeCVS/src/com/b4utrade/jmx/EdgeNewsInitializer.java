package com.b4utrade.jmx;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.b4utrade.memorydb.EdgeNewsMemoryDB;

public class EdgeNewsInitializer implements EdgeNewsInitializerMBean, MBeanRegistration{
	// Constants -----------------------------------------------------
	public static final String OBJECT_NAME = ":service=EdgeNewsInitializer";

	private static EdgeNewsMemoryDB instance;

	public ObjectName preRegister(MBeanServer server, ObjectName name)
			throws Exception
			{
		try {
			System.out.println("EdgeNewsInitializer - about to instantiate EdgeNewsMemoryDB instance!");
			instance = EdgeNewsMemoryDB.instance();
			System.out.println("EdgeNewsInitializer - successfully instantiated EdgeNewsMemoryDB instance!");
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return new ObjectName(OBJECT_NAME);
			}

	public void postRegister(Boolean registrationDone)
	{
		// empty
	}

	public void preDeregister() throws Exception
	{
		// empty
	}

	public void postDeregister()
	{
		// empty
	}
}
