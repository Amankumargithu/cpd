package com.tacpoint.threadpool;

import java.util.Properties;

// This class is used to compare the performance of 
// "pooled" Vs "non-pooled" cases.
public class TestPerformance {
	public static void main(String[] args) {
		if( args.length < 3 ) {
			System.err.println("Usage: java Test " + 
			    "<maxThreads> <minThreads> <maxIdleTime>" + 
			    " <Jobs> [debug]");
			System.exit(-1);
		}

		// Set the thread pool properties using
		// the command line args.
		Properties props = new Properties();
		props.setProperty("maxThreads",args[0]);
		props.setProperty("minThreads",args[1]);
		props.setProperty("maxIdleTime",args[2]);
		if( args.length >= 5 && args[4].equals("debug") )
			props.setProperty("debug","");         
		ThreadPool pool = new ThreadPoolImpl(props);

		if( args.length >= 4 )
			new TestPerformance(pool, 
				Integer.parseInt(args[3]));
		else
			new TestPerformance(pool, 1000);
	}

	private int count;
	private Object lock = new Object();

	public TestPerformance(ThreadPool pool, int times) {
		
		System.out.print("Performance using Pool[in ms]: ");

		// Add all the jobs. Wait for the jobs to finish 
		// and measure the time it took.
		count = 0;
		long start = System.currentTimeMillis();
		for( int i=0; i<times; i++ ) {
			pool.addJob(new Job());         
		}      
		while( true ) {
			synchronized(lock) {
				if( count == times )
					break;
			}       
			try {
				Thread.sleep(5);
			}
			catch( Exception e ) {
			}
		}

		System.out.println("" + 
		    (System.currentTimeMillis() - start));             
		
		System.out.print("Performance using no Pool[in ms]: ");

		// Start all the threads. Wait for them to finish
		// and measure the time it took.
		count = 0;
		start = System.currentTimeMillis();
		for( int i=0; i<times; i++ ) {      
			new JobThread().start();       
		}      
		while( true ) {
			synchronized(lock) {
				if( count == times )
					break;
			}       
			try {
				Thread.sleep(5);
			}
			catch( Exception e ) {
			}
		}

		System.out.println("" + 
		    (System.currentTimeMillis() - start));                   
	}

	// This class is used in the non-pooled case to start
	// each thread.
	private class JobThread extends java.lang.Thread {
		public void run() {
			synchronized(lock) {
				count++;       
			}
		}
	}

	// This claas is passed in as the "job" to the addJob
	// method on the thread pool. The only requirement for
	// this class is that it must implement java.lang.Runnable
	private class Job implements java.lang.Runnable {
		public void run() {
			synchronized(lock) {
				count++;       
			}
		}
	} 
}
