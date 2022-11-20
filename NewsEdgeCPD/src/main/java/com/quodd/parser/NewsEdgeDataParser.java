package com.quodd.parser;

import static com.quodd.controller.NewsEdgeDataController.logger;
import static com.quodd.controller.NewsEdgeDataController.newsProperties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.csvreader.CsvReader;
import com.quodd.queue.MessageQueue;
import com.quodd.util.Constants;
import com.quodd.util.CounterUtility;
import com.quodd.util.NewsBean;

public class NewsEdgeDataParser extends Thread {
	private static final String outputFolder = newsProperties.getStringProperty("outputFolder", "/data/www/newsedge");
	private static HashMap<String, String> sourceMapping = new HashMap<>();
	private static ArrayList<String> filterSourceList = new ArrayList<>();
	private static final String serverIp = newsProperties.getStringProperty("serverIP", "www.quodd.com");
	private static ArrayList<String> exchangeList = new ArrayList<>();
	private static String relFactorArray[] = null;
	private final MessageQueue inputQueue;
	private final MessageQueue outputQueue;
	private final String xmlPrefix = ".xml";
	private boolean doRun = true;
	private boolean lastRun = false;

	static {
		try {
			String sourceMappingFile = newsProperties.getStringProperty("sourceMappingFile",
					"/home/process/newsPropertieserties/SourceNameMapping.csv");
			CsvReader csvReader = new CsvReader(sourceMappingFile);
			csvReader.readHeaders();
			while (csvReader.readRecord()) {
				String sourceName = csvReader.get("SourceName");
				String serviceCode = csvReader.get("ServiceCode");
				sourceMapping.put(serviceCode, sourceName);
			}
			csvReader.close();
			logger.info("INIT hashmap of sourceCodeMapping from " + sourceMappingFile + " with record count "
					+ sourceMapping.size());
			String filterSource = newsProperties.getProperty("filterSource");
			if (filterSource == null)
				logger.warning("MISING filterSource");
			else {
				String[] filterSourceArray = filterSource.split(",");
				for (int i = 0; i < filterSourceArray.length; i++)
					filterSourceList.add(filterSourceArray[i]);
			}
			logger.info("INIT arraylist of filteredSource from newsPropertieserty:filterSource with record count "
					+ filterSourceList.size());
			String exchangeStrings = newsProperties.getProperty("exchangeFilters");
			if (exchangeStrings == null)
				logger.warning("MISISNG exchangeFilters");
			else {
				String[] exchangeFilterArray = exchangeStrings.split(",");
				for (int i = 0; i < exchangeFilterArray.length; i++)
					exchangeList.add(exchangeFilterArray[i]);
			}
			logger.info("INIT arraylist of exchangeList from newsPropertieserty:exchangeFilters  with record count "
					+ exchangeList.size());
			String relevanceFactor = newsProperties.getStringProperty("relevanceFactor", "90");
			relFactorArray = relevanceFactor.split(",");
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	public NewsEdgeDataParser(MessageQueue outputQueue, MessageQueue inputQueue) {
		this.outputQueue = outputQueue;
		this.inputQueue = inputQueue;
	}

	@Override
	public void run() {
		logger.info("Start thread " + this.getName());
		while (doRun || lastRun) {
			if (!doRun) {
				lastRun = false;
				logger.info("Thread in last run state" + this.getName());
			}
			try {
				Object[] files = inputQueue.removeAll();
				if (files == null)
					continue;
				for (Object object : files) {
					File file = (File) object;
					String fileName = file.getName();
					logger.info("PARSING " + fileName);
					try {
						InputStream inputStream = new FileInputStream(file);
						Reader reader = new InputStreamReader(inputStream, "UTF-8");
						InputSource is = new InputSource(reader);
						is.setEncoding("UTF-8");
						DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
						Document document = dBuilder.parse(is);
						document.getDocumentElement().normalize();
						NodeList list;
						list = document.getElementsByTagName("xn:resourceID");
						String storyId = list.item(0).getTextContent() + ",";
						String sourceCode = getSourceCode(storyId);
						if (filterSourceList.contains(sourceCode)) {
							logger.warning("DROP " + fileName + " Source Code is in dropped list " + sourceCode);
							continue;
						}
						boolean isPatentNews = false;
						list = document.getElementsByTagName("xn:vendorData");
						for (int i = 0; i < list.getLength(); i++) {
							String data = list.item(i).getTextContent();
							if (data.contains("Subject Code=SU/Patent")) {
								isPatentNews = true;
								break;
							}
						}
						if (isPatentNews) {
							logger.warning("DROP " + fileName + " Patent News with vendorData 'Subject Code=SU/Patent' "
									+ storyId);
							continue;
						}
						String language = "en";
						list = document.getElementsByTagName("xn:language");
						if (list.getLength() > 0)
							language = list.item(0).getTextContent();
						if (!language.equalsIgnoreCase("en") && !language.equalsIgnoreCase("lang.en")) {
							logger.warning("DROP " + fileName + " Language is not english " + language);
							continue;
						}
						String categories = "";
						list = document.getElementsByTagName("xn:subjectCode");
						for (int i = 0; i < list.getLength(); i++) {
							String subject = list.item(i).getTextContent();
							subject = subject.replaceAll("\\|", "");
							if (subject.contains("IS/appsci.patentab")) {
								isPatentNews = true;
								break;
							}
							if (subject.indexOf("#") != -1) {
								categories = categories + subject.substring(0, subject.indexOf("#")) + ",";
							} else {
								categories = categories + subject;
							}
						}
						if (isPatentNews) {
							logger.warning("DROP " + fileName + " Patent News with subjectCode 'IS/appsci.patentab' "
									+ storyId);
							continue;
						}
						list = document.getElementsByTagName("xn:industryCode");
						for (int i = 0; i < list.getLength(); i++) {
							String category = list.item(i).getTextContent();
							category = category.replaceAll("\\|", "");
							category = category.replaceAll("'", "");
							category = category.replaceAll(":", "");
							category = category.replaceAll(";", "");
							category = category.replaceAll("\\?", "");
							categories = categories + category.substring(0, category.indexOf("#")) + ",";
						}
						list = document.getElementsByTagName("xn:locationCode");
						for (int i = 0; i < list.getLength(); i++) {
							String location = list.item(i).getTextContent();
							location = location.replaceAll("\\|", "");
							location = location.replaceAll("'", "");
							location = location.replaceAll(":", "");
							location = location.replaceAll(";", "");
							location = location.replaceAll("\\?", "");
							categories = categories + location.substring(0, location.indexOf("#")) + ",";
						}
						String folderName = null;
						if (fileName.indexOf(xmlPrefix) != -1) {
							fileName = fileName.substring(0, fileName.indexOf(xmlPrefix));
							folderName = fileName.substring(0, 8);
						}
						File outpuFolder = new File(outputFolder + "//" + folderName);
						if (!outpuFolder.exists())
							outpuFolder.mkdirs();
						File outputFile = new File(outputFolder + "//" + folderName + "//" + fileName + ".html");
						if (!outputFile.exists())
							outputFile.createNewFile();
						BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
						BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
						String line;
						boolean writeToFile = false;
						list = document.getElementsByTagName("xn:publicationTime");
						String publishDate = list.item(0).getTextContent();
						publishDate = publishDate.replaceAll("\\|", "");
						publishDate = getFormatedDate(publishDate);
						String newsDate = "";
						list = document.getElementsByTagName("xn:receivedTime");
						newsDate = list.item(0).getTextContent();
						newsDate = newsDate.replaceAll("\\|", "");
						newsDate = getFormatedDate(newsDate);
						list = document.getElementsByTagName("hedline");
						String headline = list.item(0).getTextContent();
						headline = headline.replaceAll("\\n", "");
						headline = headline.replaceAll("\\|", "");
						headline = Normalizer.normalize(headline, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
						bufferedWriter.write(
								"<html><body text='#00009' link='#0000FF' alink='#0000FF' vlink='#0000FF' marginwidth='0' marginheight='0' "
										+ "topmargin='0'"
										+ " leftmargin='0' rightmargin='0' framespacing=0 frameborder='0'>"
										+ "<style='font-face:arial,helvetica,sans-serif; font-size: 12px;'>"
										+ publishDate + ", " + headline
										+ "<br><br><style='font-face:arial,helvetica,sans-serif; font-size: 11px;'>");
						String publishReason = "ORIGINAL";
						while ((line = bufferedReader.readLine()) != null) {
							if (writeToFile) {
								line = line.replaceAll("&apos;", "'");
								line = line.replaceAll("&apos", "'");
								line = line.replaceAll("&quot;", "\"");
								line = line.replaceAll("&quot", "\"");
								line = line.replaceAll("Â", "");
								line = line.replaceAll("â€œ", "\"");
								line = line.replaceAll("â€", "\"");
								line = line.replaceAll("â€™", "'");
								line = line.replaceAll("â€”", "'");
								line = line.replaceAll("<a", "<d");
								line = line.replaceAll("'", "");
								line = line.replaceAll("<!\\[CDATA\\[", "");
								line = line.replaceAll("\\]\\]>", "");
								line = line.replaceAll("\\[CDATA\\[", "");
								line = Normalizer.normalize(line, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
								if (sourceCode.equalsIgnoreCase("HUGIN___") || sourceCode.equalsIgnoreCase("APULSE__")
										|| sourceCode.equalsIgnoreCase("PR_NEWS_")
										|| sourceCode.equalsIgnoreCase("OMX_____"))// check for Thomson Reuters source
																					// and Asia Pulse Pty Ltd
								{
									line = line.replaceAll("<pre>", "<p>");
									line = line.replaceAll("</pre>", "</p>");
								}
								bufferedWriter.write(line);
							}
							if (line.contains("body.content"))
								writeToFile = true;
							if (line.contains("</body.content>"))
								writeToFile = false;
							if (line.contains("Publish Reason"))
								publishReason = line.substring(line.indexOf("=") + 1, line.indexOf("</"));
						}
						bufferedWriter.write("</body></html>");
						logger.info("Written news in " + outputFile.getAbsolutePath());
						bufferedWriter.flush();
						bufferedWriter.close();
						bufferedReader.close();
						String companyCode = "";
						StringBuffer tickerBuffer = new StringBuffer();
						try {
							list = document.getElementsByTagName("xn:companyCode");
							for (int i = 0; i < list.getLength(); i++) {
								String ticker = list.item(i).getTextContent();
								ticker = ticker.replaceAll("\\|", "");
								if (ticker == null || ticker.length() < 1)
									continue;
								String exchange = null;
								if ((ticker.indexOf(":") != -1))
									exchange = ticker.substring(0, ticker.indexOf(":"));
								if (exchange != null && exchangeList.contains(exchange)) {
									if (exchange.equalsIgnoreCase("OTC-PINK")
											|| exchange.equalsIgnoreCase("NASDAQ-OTCBB") || exchange.equals("OtherOTC")
											|| exchange.equalsIgnoreCase("NASDAQ-SMALL")
											|| sourceCode.equalsIgnoreCase("EDGAR___")) {
										if ((ticker.indexOf(":") != -1) && (ticker.indexOf("#") != -1)) {
											tickerBuffer.append(",");
											tickerBuffer.append(
													ticker.substring(ticker.indexOf(":") + 1, ticker.indexOf("#")));
										} else
											tickerBuffer.append("," + ticker);
									} else if ((ticker.indexOf(":") != -1) && (ticker.indexOf("#") != -1)) {
										for (String relFac : relFactorArray) {
											if (ticker.substring(ticker.lastIndexOf("#") + 1).equals(relFac)) {
												if (exchange.equalsIgnoreCase("Toronto")) {
													tickerBuffer.append(",");
													tickerBuffer.append(ticker.substring(ticker.indexOf(":") + 1,
															ticker.indexOf("#")));
													tickerBuffer.append(".T");
												} else if (exchange.equalsIgnoreCase("TorontoVE")) {
													tickerBuffer.append(",");
													tickerBuffer.append(ticker.substring(ticker.indexOf(":") + 1,
															ticker.indexOf("#")));
													tickerBuffer.append(".V");
												} else {
													tickerBuffer.append(",");
													tickerBuffer.append(ticker.substring(ticker.indexOf(":") + 1,
															ticker.indexOf("#")));
												}
												break;
											}
										}
									} else
										tickerBuffer.append("," + ticker);
								}
							}
							if (tickerBuffer.length() > 0)
								companyCode = tickerBuffer.substring(1);
							companyCode = companyCode.replaceAll("\\|", "");
							companyCode = companyCode.replaceAll("'", "");
							companyCode = companyCode.replaceAll(":", "");
							companyCode = companyCode.replaceAll(";", "");
							companyCode = companyCode.replaceAll("\\?", "");
						} catch (Exception e) {
							logger.log(Level.WARNING,
									"Error in parsing company code " + e.getMessage() + " filename " + fileName, e);
						}
						String title = "";
						try {
							list = document.getElementsByTagName("xn:title");
							title = list.item(0).getTextContent();
							if (title.length() > 140)
								title = title.substring(0, 140) + "....";
							title = Normalizer.normalize(title, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
							title = title.replaceAll("\\|", "");
							title = title.replaceAll("'", "");
							title = title.replaceAll(":", "");
							title = title.replaceAll("\\?", "");
							title = title.replaceAll(";", "");
						} catch (Exception exception) {
							logger.log(Level.WARNING,
									"Error in parsing title " + exception.getMessage() + " filename " + fileName,
									exception);
						}
						String newsLink = "";
						try {
							if ((fileName.lastIndexOf("//") != -1))
								newsLink = "http://" + serverIp + "/newsedge/" + folderName + "/"
										+ fileName.substring(fileName.lastIndexOf("//") + 1) + ".html";
							else
								newsLink = "http://" + serverIp + "/newsedge/" + folderName + "/" + fileName + ".html";
						} catch (Exception e) {
							logger.log(Level.WARNING,
									"Error in parsing newsLink " + e.getMessage() + " filename " + fileName, e);
						}
						String newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
						String dbDate = new SimpleDateFormat("dd-MMM-yyyy:HH:mm:ss").format(new Date());
						Date currentDate = new Date();
						float duration = 400f;
						try {
							list = document.getElementsByTagName("xn:expiryTime");
							if (list != null && (list.item(0) != null)) {
								String expiryDate = list.item(0).getTextContent();
								if (expiryDate != null && expiryDate.length() > 0) {
									duration = new Date(expiryDate.substring(0, expiryDate.indexOf("T")))
											.compareTo(currentDate);
								}
							}
						} catch (Exception e) {
							logger.log(Level.WARNING,
									"Error in parsing expirtTime " + e.getMessage() + " filename " + fileName, e);
						}
						String multicastTicker = "";
						String source = mapSource(storyId);
						if (source != null) {
							switch (source) {
							case "Benzinga":
								multicastTicker = Constants.BENZINGA_NEWS_TOPIC;
								break;
							case "Midnight Trader":
								multicastTicker = Constants.MIDNIGHT_TRADER_NEWS_TOPIC;
								break;
							case "StreetInsider":
								multicastTicker = Constants.STREET_INSIDER_NEWS_TOPIC;
								break;
							case "TheFlyOnTheWall":
								multicastTicker = Constants.THE_FLY_ON_THE_WALL_NEWS_TOPIC;
								break;

							default:
								multicastTicker = Constants.NEWS_EDGE_TOPIC;
								break;
							}
						}
						try {
							if (categories.length() > 0) {
								String categoryArray[] = categories.split(",");
								StringBuffer filteredCategory = new StringBuffer();
								for (int j = 0; j < categoryArray.length; j++) {
									if (categoryArray[j].startsWith("IS/")) {
										filteredCategory.append(",");
										filteredCategory.append(categoryArray[j]);
									} else {
										System.out.println("Skipping categoryies : " + categoryArray[j]);
									}
								}
								if (filteredCategory.length() > 0)
									categories = filteredCategory.substring(1);
								else
									categories = filteredCategory.toString();
								categories = categories.trim();
								int length = categories.length();
								if (length > 1 && categories.lastIndexOf(",") == (categories.length() - 1))
									categories = categories.substring(0, categories.length() - 1);
								categories = categories.replaceAll("\\|", "");
								categories = categories.replaceAll("'", "");
								categories = categories.replaceAll("\\?", "");
								categories = categories.replaceAll(";", "");
							}
						} catch (Exception e) {
							logger.log(Level.WARNING, "Error in parsing categories " + e.getMessage() + " filename "
									+ fileName + " categories " + categories, e);
						}
						long lastUpdateTime = System.currentTimeMillis();
						NewsBean bean = new NewsBean();
						bean.setNewsID(storyId);
						bean.setNewsDesc(title);
						bean.setNewsLink(newsLink);
						bean.setNewsDate(newsDate);
						bean.setDbDate(dbDate);
						bean.setDbDateTime(newDate);
						bean.setDuration(duration + "");
						bean.setSource(source);
						bean.setTickers(companyCode);
						bean.setCategories(categories);
						bean.setLastUpdateTime(lastUpdateTime);
						bean.setPublishReason(publishReason);
						bean.setMulticastTicker(multicastTicker);
						bean.setPublishDate(publishDate);
						outputQueue.add(bean);
						CounterUtility.incrementParserCounter();
					} catch (Exception e) {
						logger.log(Level.WARNING,
								"Parsing failed for file " + fileName + " error message - " + e.getMessage(), e);
					}
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
	}

	public void Stop() {
		doRun = false;
		lastRun = true;
		logger.info("Stop thread " + this.getName());
	}

	private String getFormatedDate(String inputDate) {
		String outputDate;
		if (inputDate == null)
			return "00-000-0000:00:00:00";
		inputDate = inputDate.replace("T", ":");
		inputDate = inputDate.substring(0, inputDate.lastIndexOf("-"));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");
		Date date = null;
		try {
			date = dateFormat.parse(inputDate);
		} catch (ParseException e) {
			logger.log(Level.WARNING, "ERROR in parsing news date " + inputDate, e);
		}
		outputDate = new SimpleDateFormat("dd-MMM-yyyy:HH:mm:ss").format(date);
		return outputDate;
	}

	private String mapSource(String storyId) {
		String source = "";
		String sourceCode = getSourceCode(storyId);
		source = sourceMapping.get(sourceCode);
		if (source == null || source.length() < 1) {
			logger.warning("No Mapping for " + sourceCode);
			source = sourceCode;
		}
		return source;
	}

	private String getSourceCode(String storyId) {
		return storyId.substring(12, 20);
	}
}