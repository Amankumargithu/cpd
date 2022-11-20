package com.quodd.common.cpd.monitoring;

import static com.quodd.common.cpd.CPD.cpdProperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.csvreader.CsvReader;
import com.quodd.common.cpd.util.FormatterUtility;
import com.quodd.common.logger.QuoddLogger;
import com.quodd.common.util.QuoddProperty;

public interface StatUtility {
	static final String EXCEPTION_STR = "Exception";
	static final String ERROR_STR = "Error";
	static final String WARNING_STR = "Warning";
	static final String DROP_STR = "Drop";
	static final String ON_DISCONNECT = "OnDisconnect";
	static final String ON_SESSION = "OnSession";
	static final String ON_CONNECT = "OnConnect";
	static final String UNKNOWN_STR = "Unknown";
	static final Logger logger = QuoddLogger.getInstance().getLogger();

	public static List<String> getCrontab() {
		ArrayList<String> crons = new ArrayList<>();
		try {
			Process proc = Runtime.getRuntime().exec("crontab -l");
			proc.waitFor();
			BufferedReader pInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = "";
			while ((line = pInput.readLine()) != null) {
				if (line.trim().length() != 0 && !line.trim().startsWith("#"))
					crons.add(line);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return crons;
	}

	public static Map<String, LinkedHashMap<String, String>> getProperties(String... propertieFiles) {
		LinkedHashMap<String, LinkedHashMap<String, String>> allProperties = new LinkedHashMap<>();
		for (String propertyFile : propertieFiles) {
			LinkedHashMap<String, String> properties = new LinkedHashMap<>();
			Set<String> keySet = null;
			QuoddProperty property = new QuoddProperty("/" + propertyFile);
			keySet = property.getAllProperty();
			for (String key : keySet)
				properties.put(key, property.getProperty(key));
			allProperties.put(propertyFile, properties);
		}
		return allProperties;
	}

	public static Map<String, String> getHelperFileDetails() {
		HashMap<String, String> map = new HashMap<>();
		String directory = cpdProperties.getStringProperty("META_FILE_DIR", null);
		if (null != directory) {
			File dir = new File(directory);
			File[] files = dir.listFiles();
			for (File file : files) {
				map.put(file.getName(), FormatterUtility.formatDate(file.lastModified()));
			}
		}
		return map;
	}

	public static List<HashMap<String, String>> getDiskInformation() {
		ArrayList<HashMap<String, String>> list = new ArrayList<>();
		try {
			Process proc = Runtime.getRuntime().exec("df -h");
			proc.waitFor();
			BufferedReader pInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String flags = "";
			int count = 0;
			String[] headings = null;
			while ((flags = pInput.readLine()) != null) {
				HashMap<String, String> valuesMap = new HashMap<>();
				flags = flags.trim().replaceAll(" +", " ");
				if (count < 1) {
					headings = flags.split(" ");
					headings[4] = headings[4].substring(0, headings[4].length() - 1);
				} else {
					String[] values = flags.split(" ");
					for (int i = 0; i < values.length; i++) {
						valuesMap.put(headings[i], values[i].replace("%", ""));
					}
					list.add(valuesMap);
				}
				count++;
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return list;
	}

	public static List<HashMap<String, String>> getRamInformation() {
		ArrayList<HashMap<String, String>> list = new ArrayList<>();
		try {
			Process proc = Runtime.getRuntime().exec("free -m");
			proc.waitFor();
			BufferedReader pInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String flags = "";
			int count = 0;
			String[] headings = null;
			while ((flags = pInput.readLine()) != null) {
				HashMap<String, String> valuesMap = new HashMap<>();
				flags = flags.trim().replaceAll(" +", " ");
				if (count < 1) {
					headings = flags.split(" ");
				} else {
					String[] values = flags.split(" ");
					for (int i = 0, j = 1; j < values.length; i++, j++) {
						valuesMap.put(headings[i], values[j]);
					}
					valuesMap.put("Name", values[0].substring(0, values[0].length() - 1));
					list.add(valuesMap);
				}
				count++;
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return list;
	}

	public static Map<String, String> getErrorCountFromLog() {
		HashMap<String, String> resultMap = new HashMap<>();
		resultMap.put(EXCEPTION_STR, "0");
		resultMap.put(ERROR_STR, "0");
		resultMap.put(WARNING_STR, "0");
		resultMap.put(DROP_STR, "0");
		resultMap.put(ON_DISCONNECT, "0");
		resultMap.put(ON_CONNECT, "0");
		resultMap.put(ON_SESSION, "0");
		resultMap.put(UNKNOWN_STR, "0");
		String directory = cpdProperties.getStringProperty("LOG_DIR", null);
		if (null != directory) {
			try {
				File dir = new File(directory);
				File[] files = dir.listFiles();
				Arrays.sort(files, (File f1, File f2) -> Long.valueOf(f2.lastModified()).compareTo(f1.lastModified()));
				updateCaseInSensitiveSearchCount(files[0], "xception", EXCEPTION_STR, resultMap);
				updateCaseInSensitiveSearchCount(files[0], "rror", ERROR_STR, resultMap);
				updateCaseInSensitiveSearchCount(files[0], "WARN", WARNING_STR, resultMap);
				updateCaseSensitiveSearchCount(files[0], "DROP", DROP_STR, resultMap);
				updateCaseSensitiveSearchCount(files[0], "OnDISCONNECT", ON_DISCONNECT, resultMap);
				updateCaseSensitiveSearchCount(files[0], "OnSESSION", ON_SESSION, resultMap);
				updateCaseSensitiveSearchCount(files[0], "OnCONNECT", ON_CONNECT, resultMap);
				updateCaseSensitiveSearchCount(files[0], "UNKNOWN", UNKNOWN_STR, resultMap);
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
		return resultMap;
	}

	public static List<String> getDetailedLog(String searchString, String position, String count,
			String isCaseSensitive) {
		ArrayList<String> resultList = new ArrayList<>();
		String directory = cpdProperties.getStringProperty("LOG_DIR", null);
		if (null != directory) {
			try {
				File dir = new File(directory);
				File[] files = dir.listFiles();
				Arrays.sort(files, (File f1, File f2) -> Long.valueOf(f2.lastModified()).compareTo(f1.lastModified()));
				Runtime rt = Runtime.getRuntime();
				String command;
				if (isCaseSensitive.equals("true")) {
					command = "grep '" + searchString + "' " + files[0].getAbsolutePath() + " | " + position + " -"
							+ count;
				} else {
					command = "grep -i '" + searchString + "' " + files[0].getAbsolutePath() + " | " + position + " -"
							+ count;
				}
				String[] cmd = { "/bin/sh", "-c", command };
				Process proc = rt.exec(cmd);
				try (BufferedReader is = new BufferedReader(new InputStreamReader(proc.getInputStream()));) {
					String line;
					while ((line = is.readLine()) != null) {
						resultList.add(line);
					}
					proc.waitFor();
					proc.destroy();
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
		return resultList;
	}

	public static void updateCaseInSensitiveSearchCount(File logFile, String searchString, String searchKey,
			Map<String, String> map) {
		try {
			Runtime rt = Runtime.getRuntime();
			String[] cmd = { "/bin/sh", "-c",
					"grep -i '" + searchString + "' " + logFile.getAbsolutePath() + " |wc -l" };
			Process proc = rt.exec(cmd);
			try (BufferedReader is = new BufferedReader(new InputStreamReader(proc.getInputStream()));) {
				String line;
				while ((line = is.readLine()) != null) {
					map.put(searchKey, line);
				}
				proc.waitFor();
				proc.destroy();
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	public static void updateCaseSensitiveSearchCount(File logFile, String searchString, String searchKey,
			Map<String, String> map) {
		try {
			Runtime rt = Runtime.getRuntime();
			String[] cmd = { "/bin/sh", "-c", "grep '" + searchString + "' " + logFile.getAbsolutePath() + " |wc -l" };
			Process proc = rt.exec(cmd);
			try (BufferedReader is = new BufferedReader(new InputStreamReader(proc.getInputStream()));) {
				String line;
				while ((line = is.readLine()) != null) {
					map.put(searchKey, line);
				}
				proc.waitFor();
				proc.destroy();
			}

		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	public static List<HashMap<String, Object>> getListOfDir(String dirPath) {
		ArrayList<HashMap<String, Object>> map = new ArrayList<>();
		try {
			dirPath = dirPath.replaceAll("__", "/");
			File dir = new File(dirPath);
			String[] dirNames = dir.list();
			for (String name : dirNames) {
				HashMap<String, Object> detailMap = new HashMap<>();
				File f = new File(dirPath + "/" + name);
				if (f.exists()) {
					detailMap.put("dirname", name);
					detailMap.put("updateTime", FormatterUtility.formatDate(f.lastModified()));
					if (f.listFiles() != null)
						detailMap.put("number of files", f.listFiles().length);
					else
						detailMap.put("number of files", 0);
					map.add(detailMap);
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return map;
	}

	public static List<HashMap<String, Object>> getDirDetails(String dirPath, String directory) {
		ArrayList<HashMap<String, Object>> map = new ArrayList<>();
		try {
			dirPath = dirPath.replaceAll("__", "/");
			File dir = new File(dirPath + "/" + directory);
			File[] files = dir.listFiles();
			if (files != null) {
				for (File file : files) {
					HashMap<String, Object> detailMap = new HashMap<>();
					detailMap.put("filename", file.getName());
					detailMap.put("updateTime", FormatterUtility.formatDate(file.lastModified()));
					detailMap.put("fileSize", file.length());
					map.add(detailMap);
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return map;
	}

	public static List<HashMap<String, Object>> getCount(String filePath) {
		ArrayList<HashMap<String, Object>> map = new ArrayList<>();
		filePath = filePath.replaceAll("__", "/");
		File file = new File(filePath);
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				HashMap<String, Object> detailMap = new HashMap<>();
				String values[] = line.split("\\s+");
				detailMap.put("filename", values[0]);
				detailMap.put("fileSize", Long.parseLong(values[1]));
				map.add(detailMap);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return map;
	}

	public static List<HashMap<String, Object>> getStatDetail(String dirPath, String folder) {
		logger.info("In StatUtility.getStatDetail");
		ArrayList<HashMap<String, Object>> map = new ArrayList<>();
		String filenames = cpdProperties.getStringProperty("FILE_NAMES", "");
		String[] files = filenames.split(",");
		try {
			dirPath = dirPath.replaceAll("__", "/");
			for (String file : files) {
				String filePath = dirPath + "/" + file + "_" + folder + ".csv";
				logger.info(() -> "StatUtility.getStatDetail filePath is " + filePath);
				File fileObj = new File(filePath);
				if (fileObj.exists()) {
					CsvReader csvReader = new CsvReader(filePath, '|');
					while (csvReader.readRecord()) {
						HashMap<String, Object> data = new HashMap<>();
						data.put("fileName", csvReader.get(0));
						data.put("fileSize", Long.parseLong(csvReader.get(1)));
						data.put("details", csvReader.get(2));
						logger.finest(() -> "Data: " + data);
						map.add(data);
					}
					csvReader.close();
				} else {
					logger.warning(() -> "File does not exist " + filePath);
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return map;
	}
}
