package ntp.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;

import ntp.logger.NTPLogger;

public class OptionsRegionalExchangesPopulator {

	private HashMap<String,String> exchangeMap = new HashMap<String,String>();
	private static volatile OptionsRegionalExchangesPopulator exchangeMapping = new OptionsRegionalExchangesPopulator();
	private WatchService watcher;
	private String exchangeFileDir;
	private String exchangeFileName;

	private OptionsRegionalExchangesPopulator() {
		exchangeFileDir = CPDProperty.getInstance().getProperty("EXCHANGE_FILE_DIR");
		if(exchangeFileDir == null)
		{
			NTPLogger.missingProperty("EXCHANGE_FILE_DIR");
			exchangeFileDir = "/home/exchangeFile";
			NTPLogger.defaultSetting("EXCHANGE_FILE_DIR", exchangeFileDir);
		}
		exchangeFileName = CPDProperty.getInstance().getProperty("EXCHANGE_FILE_NAME");
		if(exchangeFileName == null)
		{
			NTPLogger.missingProperty("EXCHANGE_FILE_NAME");
			exchangeFileName = "regionalExchanges.properties";
			NTPLogger.defaultSetting("EXCHANGE_FILE_NAME", exchangeFileName);
		}
		loadFile();
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					startWatcherService(exchangeFileDir);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, "OpRegFileWatcher");
		t.start();
	}

	public static final OptionsRegionalExchangesPopulator getDefaultInstance() {
		return exchangeMapping;
	}

	public void loadFile() {
		try
		{
			exchangeMap.clear();
			BufferedReader reader = new BufferedReader(new FileReader(exchangeFileDir + "/" + exchangeFileName));
			String line = null;
			while( (line = reader.readLine()) != null)
			{
				String[] arr = line.split(",");
				if(arr.length >= 2)
					exchangeMap.put(arr[0], arr[1]);
			}
			reader.close();
			/*InputStream in = getClass().getResourceAsStream("/regionalExchanges.properties");
			Properties prop = new Properties();
			prop.load(in);
			String exchanges = prop.getProperty("EXCHANGES");
			StringTokenizer token = new StringTokenizer (exchanges, ">");
			while (token.hasMoreTokens())
			{
				String aLine = token.nextToken();
				StringTokenizer elem = new StringTokenizer (aLine, "||");
				String exchName = elem.nextToken();
				String exchCode = elem.nextToken();
				exchangeMap.put(exchName, exchCode);
			}				*/
			NTPLogger.info("Exchanges: "+exchangeMap);
		}
		catch (Exception e)
		{
			NTPLogger.error("Regional Exchange File Not found in path - " + exchangeFileDir + "/" + exchangeFileName);
			e.printStackTrace();
		}
	}

	private void startWatcherService(String dir) throws Exception{
		try {
			NTPLogger.info("starting watcher service in OptionsRegionalExchangesPopulator");
			watcher = FileSystems.getDefault().newWatchService();			
			Path path = Paths.get(dir);	
			path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY); 
			while(true)
			{
				try 
				{
					WatchKey key = watcher.take();
					for (WatchEvent event : key.pollEvents())
					{
						loadFile();
					}
					boolean valid = key.reset();
					if (!valid)
					{
						NTPLogger.info("Exiting the watcher service in OptionsRegionalExchangesPopulator");
						break;
					}
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public HashMap<String,String> getOptionsRegionalExchanges(){
		return exchangeMap;
	}
}
