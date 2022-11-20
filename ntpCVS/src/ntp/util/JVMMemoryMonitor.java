package ntp.util;

import ntp.logger.NTPLogger;

public class JVMMemoryMonitor implements Runnable
{
	@Override
	public void run() 
	{
		try // IN case there is any error bcs of Runtime class
		{
			int mb = 1024*1024;
			Runtime runtime = Runtime.getRuntime();
			long startTime = System.currentTimeMillis();
			long timeInterval = 15000;
			try 
			{
				timeInterval = Long.parseLong(CPDProperty.getInstance().getProperty("TIME_INTERVAL"));
			} 
			catch (Exception e) {
				NTPLogger.missingProperty("TIME_INTERVAL");
				timeInterval = 15000;
				NTPLogger.defaultSetting("TIME_INTERVAL", "" +timeInterval);
			}
			while(true)
			{
				try 
				{
					Thread.sleep(1000);
					if(System.currentTimeMillis() - startTime > timeInterval)
					{
						System.out.println("#############################################");
						NTPLogger.info("##### JVM memory utilization statistics #####");
						float usedMemory = runtime.totalMemory() - runtime.freeMemory() ;	      
						System.out.println("Used Memory: " + usedMemory /mb );							
						System.out.println("Free Memory: "+ runtime.freeMemory() / mb);	       		
						System.out.println("Total Memory: " + runtime.totalMemory() / mb);	        
						System.out.println("Max Memory: " + runtime.maxMemory() / mb);
						System.out.println("#############################################");
						startTime = System.currentTimeMillis();
					}
				} 
				catch (Exception e){}
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		catch (Error error) 
		{
			error.printStackTrace();
		}
	}
}
