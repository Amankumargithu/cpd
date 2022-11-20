package ntp.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import com.google.gson.Gson;

public class ReportGenerator {

	private File reportFile = null;

	public ReportGenerator(String filepath){
		if(filepath != null && filepath.length() > 0)
		{
			reportFile = new File(filepath);
			File dirFile = reportFile.getParentFile();
			if(dirFile != null && !dirFile.exists())
				dirFile.mkdirs();
			if(!reportFile.exists())
				try {
					reportFile.createNewFile();
					NTPLogger.info("Report will get generated in " + filepath);
				} catch (IOException e) {
					NTPLogger.error("Excpetion while creating reporting file " + e.getMessage());
					e.printStackTrace();
				}
		}
		else
			NTPLogger.error("No file path for report generation " + filepath);
	}

	public void generateReport(HashMap outputMap)
	{
		if(reportFile != null && outputMap != null)
		{
			try {
				FileWriter writer = new FileWriter(reportFile, true);
				Gson gson = new Gson();
				writer.write(gson.toJson(outputMap) + "\n");
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
