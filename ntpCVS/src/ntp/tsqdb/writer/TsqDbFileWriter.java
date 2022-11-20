package ntp.tsqdb.writer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import ntp.logger.NTPLogger;

public class TsqDbFileWriter implements Runnable{

	private Vector<byte[]> tsqArray = new Vector<byte[]>();
	private boolean isRunning = false;
	private File dbFile;
	private long buffersize = 32 * 1024 * 1024;	//32 MB
	private long currentByteWrite = 0;
	private MappedByteBuffer wrBuf;
	private FileChannel rwChannel;
	private RandomAccessFile rwfile;
	private long fileSize = 0;
	private Object obj = new Object();
	private int counterMsgs = 0;
	private SimpleDateFormat sd =  new SimpleDateFormat("yyyyMMddHHmmssSSS");
	private String fileDir;
	private int thresholdProcessingTime;
	private int thresholdRecords;
	private long startTime;
	private int fileType = 0;

	public TsqDbFileWriter(String fileDir, int thresholdRecords, int thresholdTime, int fileType) {
		this.fileDir = fileDir;
		this.thresholdRecords = thresholdRecords;
		this.thresholdProcessingTime = thresholdTime;
		this.fileType = fileType;
	}

	public void add(byte[] s)
	{
		synchronized (obj) {
			tsqArray.add(s);
		}
	}

	@Override
	public void run() {
		if(!isRunning)
			isRunning = true;
		renewFile();
		while(isRunning)
		{
			checkFileThreshold();
			if(tsqArray.size() > 0)
			{
				Vector<byte[]> temp = null;
				synchronized (obj) {
					temp = tsqArray;
					tsqArray = new Vector<byte[]>();					
				}
				if(temp != null)
				{
					NTPLogger.info("temp array size is : "+temp.size());
					for(byte[] o : temp)
					{
						try {
							save(o);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} else
			{
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if(tsqArray != null && tsqArray.size() > 0)
		{
			for(byte[] o : tsqArray)
			{
				try {
					save(o);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		try
		{
			closeFile();
			if(counterMsgs == 0)
				dbFile.delete();
			else
				dbFile.renameTo(new File(dbFile.getAbsolutePath()+".C"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		NTPLogger.info("Closing " + dbFile.getName());
	}

	public void stopThread()
	{
		isRunning = false;
	}

	private void save(byte[] byteArr) throws Exception {
		counterMsgs++;
		if(currentByteWrite + byteArr.length < buffersize)
		{
			wrBuf.put(byteArr);
			currentByteWrite += byteArr.length;
		}
		else
		{
			NTPLogger.info("Reallocating memory to " + dbFile.getName());
			closeFile();
			createNewFile( dbFile.length());
			if(currentByteWrite + byteArr.length < buffersize)
			{
				wrBuf.put(byteArr);
				currentByteWrite += byteArr.length;
			}
		}
	}

	private void createNewFile(long length){
		try {
			rwfile = new RandomAccessFile(dbFile, "rw");
			rwfile.seek(dbFile.length());
			rwChannel = rwfile.getChannel();
			wrBuf = rwChannel.map(FileChannel.MapMode.READ_WRITE, length, buffersize);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void closeFile(){
		try {
			wrBuf.force();
			fileSize += currentByteWrite;
			rwfile.setLength(fileSize);
			rwChannel.close();
			currentByteWrite = 0;
			wrBuf.clear();
			rwfile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void checkFileThreshold(){
		long currentTime = System.currentTimeMillis();
		long diff = currentTime -startTime;
		if(counterMsgs > thresholdRecords || diff >= thresholdProcessingTime){
			NTPLogger.info("tsqArray size is : "+tsqArray.size());
			if(counterMsgs == 0)
				dbFile.delete();
			counterMsgs = 0;
			closeFile();
			dbFile.renameTo(new File(dbFile.getAbsolutePath()+".C"));
			renewFile();
		}
	}

	private void renewFile() {
		String fileName = null;
		String currentDate = sd.format(new Date());
		if(fileType == 0)
			fileName = fileDir+"tsq"+currentDate+"_t.csv";
		else if(fileType == 1)
			fileName = fileDir+"tsq"+currentDate+"_q.csv";
		else if(fileType == 2)
			fileName = fileDir+"tsq"+currentDate+"_cxl.csv";
		startTime = System.currentTimeMillis();
		dbFile = new File(fileName);
		fileSize = 0;
		createNewFile(0);
	}
}