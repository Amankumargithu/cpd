package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.csvreader.CsvReader;
import com.tacpoint.exception.BusinessException;
import com.tacpoint.util.Environment;

public class GetOptionsStat extends B4UTradeDefaultAction {
	static Log log = LogFactory.getLog(GetOptionsStat.class);

	private HashMap<String, Object> map = null;
	private WatchService watcher;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		try {
			response.setContentType("text/xml");
			try {
				if (map == null) {
					Environment.init();
					final String dir = Environment.get("OPRAVOLUMES_PATH");
					log.info("In  execute dir is " + dir);
					if (dir == null) {
						log.info("OPRAVOLUMES_PATH not set in environment.properties");
					}
					loadDataFromFile(dir);
					Thread t = new Thread(() -> startWatcherService(dir), "OptionStatFileWatcher");
					t.start();
				}
				ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
				try (XMLEncoder encoder = new XMLEncoder(byteArray);) {
					encoder.writeObject(map);
				}
				try (ServletOutputStream sos = response.getOutputStream();) {
					sos.write(byteArray.toByteArray());
					sos.flush();
				} catch (Exception e) {
					log.error("GetOptionsStat.execute() : servlet output stream encountered exception. ", e);
				}
			} catch (Exception e) {
				log.error("GetOptionsStat - db exception occurred. ", e);
				throw new BusinessException("GetOptionsStat - db error occurred. ", e);
			}
		} catch (Exception e) {
			log.error("GetOptionsStat ", e);
			return null;
		}
		return null;
	}

	private void startWatcherService(String dir) {
		try {
			log.info("In start watcher service in GetOptionsStat");
			watcher = FileSystems.getDefault().newWatchService();
			log.info("In start watcher service in GetOptionsStat watcher is  " + watcher);
			Path path = Paths.get(dir);
			path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
			while (true) {
				try {
					WatchKey key = watcher.take();
					for (WatchEvent event : key.pollEvents()) {
						loadDataFromFile(dir);
					}
					boolean valid = key.reset();
					if (!valid) {
						log.info("Exiting the watcher service");
						break;
					}
				} catch (Exception e) {
					log.error("GetOptionsStat ", e);
				}
			}
		} catch (IOException e) {
			log.error("GetOptionsStat ", e);
		}
	}

	private void loadDataFromFile(String dir) {
		log.info("In loadDataFromFile in GetOptionsStat");
		map = new HashMap<>();
		String filename = "OPRAVolumes.out";
		CsvReader reader = null;
		try {
			reader = new CsvReader(dir + "/" + filename);
			reader.readRecord();
			map.put(reader.get(0), reader.get(1));
			reader.readRecord();
			HashMap<String, String> stats = new LinkedHashMap<>();
			while (reader.readRecord() && !reader.get(0).contains("TOTAL")) {
				stats.put(reader.get(0), reader.get(1));
			}
			List<Map.Entry<String, String>> list = new LinkedList<Map.Entry<String, String>>(stats.entrySet());
			Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
				public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
					return (o1.getValue()).compareTo(o2.getValue());
				}
			});
			map.put("Stats", stats);
			while (reader.readRecord()) {
				if (reader.get(0).equals("Contract")) {
					break;
				}
			}
			HashMap<String, String> topContracts = new LinkedHashMap<>();
			while (reader.readRecord()) {
				topContracts.put(reader.get(0), reader.get(1));
			}
			map.put("TopContracts", topContracts);
			reader.close();
		} catch (Exception e) {
			log.error("GetOptionsStat ", e);
		} finally {
			if (reader != null)
				reader.close();
		}
	}
}
