package ntp.indicesmf;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ntp.bean.QTCPDMessageBean;
import ntp.logger.NTPLogger;
import ntp.queue.VectorMessageQueue;
import ntp.util.CPDProperty;

public class BondsDataFileWriter extends Thread 
{
	private boolean doRun = true;

	public boolean isDoRun() {
		return doRun;
	}

	public void setDoRun(boolean doRun) {
		this.doRun = doRun;
	}

	@Override
	public void run()
	{
		try 
		{
			String bondsDataFile = CPDProperty.getInstance().getProperty("BONDS_DATA_FILE");
			if (bondsDataFile == null)
			{
				NTPLogger.missingProperty("BONDS_DATA_FILE");
				bondsDataFile = "/home/mf_cpd/bondsData";
				NTPLogger.defaultSetting("BONDS_DATA_FILE", bondsDataFile);
			}
			DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
			bondsDataFile = bondsDataFile + "_" +formatter.format(new Date()) + ".csv";
			NTPLogger.info("Going to write bonds data to file: " + bondsDataFile);
			FileOutputStream fos = new FileOutputStream(new File(bondsDataFile));
			StringBuilder header = new StringBuilder();
			header.append("Ticker,Last price,Ask price,Bid price,Size,Volume\n");			
			fos.write(header.toString().getBytes());
			fos.flush();
			VectorMessageQueue queue = IndicesMFQTMessageQueue.getInstance().getBondsWriterQueue();
			while (doRun)
			{
				try 
				{
					Object [] objs = queue.removeAll();
					for (int i = 0; i < objs.length; i++) {
						QTCPDMessageBean bean = (QTCPDMessageBean) objs[i];
						String ticker = bean.getTicker();
						StringBuilder line = new StringBuilder();
						line.append(ticker).append(",");
						line.append(bean.getLastPrice()).append(",");
						line.append(bean.getAskPrice()).append(",");
						line.append(bean.getBidPrice()).append(",");
						line.append(bean.getBidSize()).append("x");
						line.append(bean.getAskSize()).append(",");
						line.append(bean.getVolume());
						line.append("\n");
						System.out.print(new Timestamp(System.currentTimeMillis()) + " Going to write to file: " + line.toString());
						fos.write(line.toString().getBytes());
						fos.flush();
					}
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
			fos.close();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
