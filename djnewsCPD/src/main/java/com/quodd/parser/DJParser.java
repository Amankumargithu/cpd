package com.quodd.parser;

import static com.quodd.controller.DJController.cpdProperties;
import static com.quodd.controller.DJController.logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.quodd.bean.NewsBean;
import com.quodd.common.logger.CommonLogMessage;
import com.quodd.controller.DJController;
import com.quodd.populator.DJPopulator;
import com.quodd.util.Constants;
import com.quodd.util.HTMLNewsWriter;

public class DJParser extends Thread {
	private boolean doRun = false;
	private HTMLNewsWriter htmlNewsWriter = null;
	private String djFeedId = null;
	private DJPopulator djDistributor = null;
	private String newsDnsHeader = null;
	private String newsDirectory = null;
	private final String newsPrefix = "/news/djnews/";
	private HashMap<String, String> productList = new HashMap<>();
	private HashMap<String, String> newsLinkMap = new HashMap<>();
	private HashMap<Long, String> accessionMap = new HashMap<>();
	private ConcurrentLinkedQueue<byte[]> msgQueue = new ConcurrentLinkedQueue<>();
	private DateTimeFormatter oldPattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private DateTimeFormatter newPattern = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mma");
	private Connection mysqlCon;
	private NewsBean newsBean = null;

	public DJParser(DJPopulator distributor, Connection con) {
		this.djDistributor = distributor;
		this.mysqlCon = con;
		this.doRun = true;
		String newsDir = cpdProperties.getStringProperty("NEWS_DIR", "/var/www/djnews");
		this.djFeedId = cpdProperties.getStringProperty("FEED_ID", "NIP1");// to check if destination id is correct
		this.newsDnsHeader = cpdProperties.getStringProperty("NEWS_DNS_HEADER", "http://www.quodd.com/djnews/");
		String dateLink = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
		this.newsDnsHeader = this.newsDnsHeader + dateLink;
		this.productList.put("DN", "01");
		this.productList.put("SN", "02");
		this.productList.put("AD", "03");
		this.htmlNewsWriter = new HTMLNewsWriter(newsDir);
		this.newsDirectory = newsDir + "/" + dateLink;
		File dir = new File(this.newsDirectory);
		if (!dir.exists()) {
			logger.info("Creating directories " + dir.getAbsolutePath());
			dir.mkdirs();
		}
	}

	public void add(byte[] msg) {
		this.msgQueue.add(msg);
	}

	@Override
	public void run() {
		try {
			logger.info(CommonLogMessage.startThread(this.getName()));
			while (this.doRun) {
				byte[] vData = this.msgQueue.poll();
				if (vData != null) {
					ByteBuffer buffer = ByteBuffer.wrap(vData);
					buffer.order(ByteOrder.LITTLE_ENDIAN);
					parseMessage(buffer);
				} else
					TimeUnit.MILLISECONDS.sleep(100);
			}
			while (!this.msgQueue.isEmpty()) {
				byte[] vData = this.msgQueue.poll();
				if (vData != null) {
					ByteBuffer buffer = ByteBuffer.wrap(vData);
					buffer.order(ByteOrder.LITTLE_ENDIAN);
					parseMessage(buffer);
				}
			}
		} catch (Exception | Error e) {
			logger.log(Level.WARNING, this.getName() + " " + e.getMessage(), e);
		}
	}

	private void parseMessage(ByteBuffer buffer) {
		try {
			DJController.totalNews++;
			byte msgStart = buffer.get();
			byte msgType = buffer.get();
			byte headerLength = buffer.get();
			byte msgVersion = buffer.get();
			if (msgStart != 0x01 || msgType != 0x11 || headerLength != 0x0E || msgVersion != 02)
				logger.warning(
						"MISMATCH HEADER-MSG " + msgStart + "," + msgType + "," + headerLength + "," + msgVersion);
			int msgLength = buffer.getShort();
			int mdStart = buffer.getShort(); // Message Distribution starting position
			int ipStart = buffer.getShort(); // Information Processing starting position
			int tdStart = buffer.getShort();// Text Data starting position
			int sdStart = buffer.getShort();// SD starting position - will always 0
			this.newsBean = new NewsBean();
			boolean isOK = false;
			int limit = ipStart - mdStart;
			if (mdStart != 0 && limit > 0) {
				buffer.position(mdStart);
				ByteBuffer mdBuffer = buffer.slice();
				mdBuffer.limit(limit);
				mdBuffer.order(ByteOrder.LITTLE_ENDIAN);
				isOK = parseMessageDistribution(mdBuffer);
			}
			if (!isOK) {
				DJController.dropCount++;
				logger.warning("DROP NEWS " + this.newsBean.toString());
				return;
			}
			limit = tdStart - ipStart;
			if (ipStart != 0 && limit > 0) {
				buffer.position(ipStart);
				ByteBuffer ipBuffer = buffer.slice();
				ipBuffer.limit(limit);
				ipBuffer.order(ByteOrder.LITTLE_ENDIAN);
				isOK = parseInformationProcessing(ipBuffer);
			}
			if (!isOK) {
				DJController.dropCount++;
				logger.warning("DROP NEWS " + this.newsBean.toString());
				return;
			}
			if (tdStart != 0) {
				buffer.position(tdStart);
				ByteBuffer tdBuffer = buffer.slice();
				tdBuffer.order(ByteOrder.LITTLE_ENDIAN);
				parseTextData(tdBuffer);
			}
			String sourceId = this.productList.get(this.newsBean.getProduct());
			if (sourceId == null) {
				logger.warning(this.getName() + " No source id for " + this.newsBean.getProduct());
				sourceId = "11";
			}
			this.newsBean.setNewsId(this.newsBean.getDocdate() + sourceId + this.newsBean.getSeq());
			logger.info("NEWS_ID is : " + this.newsBean.getNewsId());
			if (this.newsBean.getMsgType() != null) {
				if ("S".equals(this.newsBean.getMsgType())) {
					DJController.dropCount++;
					logger.warning("DROP NEWS " + this.newsBean.toString());
					return;
				}
				if ("C".equals(this.newsBean.getMsgType())) {
					// Handle update and delete news
					String actionCode = this.newsBean.getActionCode();
					if (actionCode == null || "XB".equals(actionCode)) {
						DJController.dropCount++;
						logger.warning("DROP NEWS " + this.newsBean.toString());
						return;
					}
					if (actionCode.contains("H") || actionCode.contains("N") || actionCode.contains("T")
							|| "XA".equals(actionCode)) {
						String newsLink = this.newsLinkMap.get(this.newsBean.getNewsId());
						if (newsLink != null) {
							this.newsBean.setNewsLink(newsLink);
						} else {
							try (Statement stmt1 = this.mysqlCon.createStatement();) {
								String query = "SELECT NEWS_LINK FROM DJ_NEWS_INFO where NEWS_ID = "
										+ this.newsBean.getNewsId();
								logger.info(this.getName() + " Query " + query);
								try (ResultSet rs = stmt1.executeQuery(query);) {
									if (rs.next()) {
										this.newsBean.setNewsLink(rs.getString(1));
									}
								}
							} catch (Exception e) {
								logger.log(Level.WARNING, this.getName() + " " + e.getMessage(), e);
							}
						}
						if ("XA".equals(actionCode)) {
							processFileDelete(this.newsBean);
						} else
							processFileUpdation(this.newsBean);
					}
				} else if ("N".equals(this.newsBean.getMsgType())) {
					boolean doCreateFile = true;
					String newslink = this.accessionMap.get(this.newsBean.getAccessionNumber());
					if (newslink != null) {
						this.newsBean.setNewsLink(newslink);
						doCreateFile = false;
					} else {
						long accessionNumber = this.newsBean.getAccessionNumber();
						try (Statement stmt1 = this.mysqlCon.createStatement();) {
							String query = "select NEWS_LINK from DJ_NEWS_INFO where ACCESSION_NUM = " + accessionNumber
									+ " order by news_id desc ";
							logger.info(this.getName() + " Query " + query);
							try (ResultSet rs = stmt1.executeQuery(query);) {
								if (rs.next()) {
									doCreateFile = false;
									this.newsBean.setNewsLink(rs.getString(1));
									this.accessionMap.put(accessionNumber, this.newsBean.getNewsLink());
								}
							}
						} catch (Exception e) {
							logger.log(Level.WARNING, this.getName() + " " + e.getMessage(), e);
						}
					}
					if (doCreateFile)
						saveStory(this.newsBean);
					else
						processFileMerge(this.newsBean);
				}
				this.djDistributor.add(this.newsBean);
			} else {
				DJController.dropCount++;
				logger.warning("DROP MessageType is null " + this.newsBean.toString());
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, this.getName() + " " + e.getMessage(), e);
		}
	}

	private boolean parseMessageDistribution(ByteBuffer buffer) {
		try {
			byte mdStart = buffer.get();
			byte mdType = buffer.get();
			int headerLength = buffer.get();
			byte mdVersion = buffer.get();
			if (mdStart != 0x02 || mdType != 0x12 || headerLength != 0x04 || mdVersion != 01)
				logger.warning("MISMATCH HEADER-MD " + mdStart + "," + mdType + "," + headerLength + "," + mdVersion);
			byte[] bytes = new byte[4];
			buffer.get(bytes);
			String originAddr = new String(bytes).trim(); // neglect Origin address - it is constant
			bytes = new byte[4];
			buffer.get(bytes);
			String destinationAddr = new String(bytes).trim();
			if (!this.djFeedId.equals(destinationAddr)) {
				logger.warning("parseMessageDistribution() DJ feed Id mismatch configured " + this.djFeedId
						+ " receiving " + destinationAddr);
				return false;
			}
			bytes = new byte[2];
			buffer.get(bytes);
			String product = new String(bytes).trim();
			this.newsBean.setProduct(product);
			if (!this.productList.containsKey(product)) {
				logger.warning("parseMessageDistribution() Not entitled for product " + this.newsBean.getProduct());
				return false;
			}
			bytes = new byte[2];
			buffer.get(bytes);
			String service = new String(bytes).trim();
			bytes = new byte[8];
			buffer.get(bytes);
			String docDate = new String(bytes).trim();
			this.newsBean.setDocdate(docDate);
			bytes = new byte[6];
			buffer.get(bytes);
			String seq = new String(bytes).trim();
			this.newsBean.setSeq(seq);
			bytes = new byte[6];
			buffer.get(bytes);
			String transmissionTime = new String(bytes).trim();
			char operationClass = (char) buffer.get();
			char operationMode = (char) buffer.get();
			bytes = new byte[4];
			buffer.get(bytes);
			String milliSec = new String(bytes).trim();
			if ("CO".equals(service)) {
				return true;
			} else {
				logger.warning("parseMessageDistribution() Service not matched got service - " + service);
				return false;
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, this.getName() + " " + e.getMessage(), e);
		}
		return false;
	}

	private boolean parseInformationProcessing(ByteBuffer buffer) {
		try {
			byte ipStart = buffer.get();
			byte ipType = buffer.get();
			switch (ipType) {
			case 0x1B: {
				processIpNews(buffer);
				if (this.newsBean.getCategories().size() < 7)
					return false;
				return checkNewsCategories(this.newsBean);
			}
			case 0x1C: {
				boolean isIpBody = processIpCommand(buffer);
				if (!isIpBody)
					return true;
				if (this.newsBean.getCategories().size() < 7)
					return false;
				return checkNewsCategories(this.newsBean);
			}
			case 0x1D: {
				processExpiration(buffer);
			}
				break;
			case 0x14: {
				processSystem(buffer);
			}
				break;
			case 0x17:
				processSymbolMaintainence(buffer);
				break;
			default:
				logger.warning("parseInformationProcessing unknown message type " + ipType);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, this.getName() + " " + e.getMessage(), e);
		}
		return true;
	}

	private void parseTextData(ByteBuffer buffer) {
		try {
			byte textStart = buffer.get();
			byte textType = buffer.get();
			int headerLength = buffer.get();
			byte textVersion = buffer.get();
			if (textStart != 0x04 || textType != 0x11 || headerLength != 0x16 || textVersion != 03)
				logger.warning(
						"MISMATCH HEADER-TD " + textStart + "," + textType + "," + headerLength + "," + textVersion);
			int structureIndex = buffer.getShort();
			buffer.limit(structureIndex);
			int hdlnIndex = buffer.getShort();
			int hdlnLen = buffer.getShort();
			int storyIndex = buffer.getShort();
			int storyLen = buffer.getShort();
			int timeIndex = buffer.getShort();
			int timeLength = buffer.getShort();
			int summaryIndex = buffer.getShort();
			int summaryLength = buffer.getShort();
			if (hdlnIndex != 0 || storyIndex != 0 || timeLength != 0) {
				int bellCount = buffer.get();
				byte tabIndicator = buffer.get();
				byte pressCutoutIndicator = buffer.get();
				byte fieldSeparator = buffer.get(hdlnIndex + hdlnLen);
			}
			if (hdlnIndex != 0) {
				byte[] bytes = new byte[hdlnLen];
				buffer.position(hdlnIndex);
				buffer.get(bytes);
				String headline = new String(bytes).trim();
				if (headline != null) {
					if (headline.contains("'"))
						headline = headline.replaceAll("'", "''");
					if (headline.contains(";"))
						headline = headline.replaceAll(";", "'';");
					if (headline.contains("|"))
						headline = headline.replaceAll("\\|", "");
					if (headline.contains("?"))
						headline = headline.replaceAll("\\?", "");
				}
				this.newsBean.setHeadline(headline);
			}
			if (storyIndex != 0) {
				buffer.position(storyIndex);
				StringBuilder sb = new StringBuilder();
				int gsIndex = 0;
				int bsIndex = 0;
				int length = storyLen + storyIndex;
				boolean bsFlag = false;
				for (int i = buffer.position(); i < length; i++) {
					byte val = buffer.get();
					if (val == 0x08)// encountered back space byte
					{
						if (bsIndex == 0)
							sb.append("<pre>");
						sb.append("<br>");
						bsFlag = true;
						bsIndex++;
					} else if (val == 0x1D)// encountered group separator byte
					{
						if (bsFlag)
							sb.append("</pre>");
						if (gsIndex == 0)
							sb.append("<p>");
						else
							sb.append("</p><p>");
						bsFlag = false;
						gsIndex++;
					} else {
						sb.append((char) val);
					}
				}
				sb.append("</p>");
				this.newsBean.setBody(sb.toString());
			}
			if (timeIndex != 0) {
				// TODO implement
			}
			if (summaryIndex != 0) {
				// TODO implement
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, this.getName() + " " + e.getMessage(), e);
		}
	}

	private void processIpNews(ByteBuffer buffer) {
		int headerLength = buffer.get();
		byte ipVersion = buffer.get();
		if (headerLength != 0x07 || ipVersion != 01)
			logger.warning("MISMATCH HEADER-IP-NEWS " + headerLength + "," + ipVersion);
		int structureLength = buffer.getShort();
		buffer.limit(structureLength);
		char msgType = (char) buffer.get();
		this.newsBean.setMsgType(Character.toString(msgType));
		processIpBody(buffer);
	}

	private boolean processIpCommand(ByteBuffer buffer) {
		int headerLength = buffer.get();
		byte ipVersion = buffer.get();
		if (headerLength != 0x1B || ipVersion != 01)
			logger.warning("MISMATCH HEADER-IP-CMD " + headerLength + "," + ipVersion);
		int structureLength = buffer.getShort();
		buffer.limit(structureLength);
		char msgType = (char) buffer.get();
		this.newsBean.setMsgType(Character.toString(msgType));
		byte[] bytes = new byte[2];
		buffer.get(bytes);
		String actionCode = new String(bytes).trim();
		bytes = new byte[2];
		buffer.get(bytes);
		String product = new String(bytes).trim();
		this.newsBean.setProduct(product);
		bytes = new byte[2];
		buffer.get(bytes);
		String refService = new String(bytes).trim();
		bytes = new byte[8];
		buffer.get(bytes);
		String docDate = new String(bytes).trim();
		this.newsBean.setDocdate(docDate);
		bytes = new byte[6];
		buffer.get(bytes);
		String seq = new String(bytes).trim();
		this.newsBean.setSeq(seq);
		switch (actionCode) {
		case "I":
		case "IH":
		case "IT":
		case "IN":
			processIpBody(buffer);
			return true;
		case "H":
		case "T":
		case "N": {
			int sectionLength = buffer.getShort();
			char tempPerm = (char) buffer.get();
			this.newsBean.setTempPerm(Character.toString(tempPerm));
		}
			break;
		case "XA":
		case "XB":
			break;
		default:
			logger.warning("processIpCommand unknown action code " + actionCode);
			break;
		}
		return false;
	}

	private void processExpiration(ByteBuffer buffer) {
		int headerLength = buffer.get();
		byte ipVersion = buffer.get();
		if (headerLength != 0x07 || ipVersion != 01)
			logger.warning("MISMATCH HEADER-IP-EXP " + headerLength + "," + ipVersion);
		int structureLength = buffer.getShort();
		buffer.limit(structureLength);
		char msgType = (char) buffer.get();
		this.newsBean.setMsgType(Character.toString(msgType));
		int expiredLen = buffer.getShort();
		int listCount = buffer.get();
		if (listCount == 0) {
			logger.warning("Expiration list count is 0 ");
			return;
		}
		byte reserved = buffer.get();
		int remainingCount = buffer.remaining();
		if (remainingCount + 4 != expiredLen)
			logger.warning("MISMATCH Buffer data length " + remainingCount + "," + expiredLen);
		byte[] bytes = new byte[remainingCount];
		buffer.get(bytes);
		int index = 0;
		int prvIndex = index;
		while (index < expiredLen) {
			if (bytes[index] != 0x1C)
				index++;
			else {
				if (index == prvIndex)
					this.newsBean.getExpireList().add(null);
				else {
					String expiredDetail = new String(bytes, prvIndex, index - prvIndex);
					String product = expiredDetail.substring(0, 2);
					String sourceId = this.productList.get(product);
					if (sourceId == null)
						sourceId = "11";
					String newsId = expiredDetail.substring(2, 10) + sourceId + expiredDetail.substring(10);
					this.newsBean.getExpireList().add(newsId);
				}
				index++;
				prvIndex = index;
			}
		}
	}

	private void processSystem(ByteBuffer buffer) {
		int headerLength = buffer.get();
		byte ipVersion = buffer.get();
		if (headerLength != 0x07 || ipVersion != 01)
			logger.warning("MISMATCH HEADER-IP-SYS " + headerLength + "," + ipVersion);
		int structureLength = buffer.getShort();
		buffer.limit(structureLength);
		char msgType = (char) buffer.get();
		this.newsBean.setMsgType(Character.toString(msgType));
		int sectionLength = buffer.getShort();
		byte[] bytes = new byte[sectionLength - 2];
		buffer.get(bytes);
		String text = new String(bytes).trim();
		logger.warning("UNPROCESSED system_IP" + msgType + "," + text);
	}

	private void processSymbolMaintainence(ByteBuffer buffer) {
		int headerLength = buffer.get();
		byte ipVersion = buffer.get();
		if (headerLength != 0x0F || ipVersion != 01)
			logger.warning("MISMATCH HEADER-IP-SYM " + headerLength + "," + ipVersion);
		int structureLength = buffer.getShort();
		buffer.limit(structureLength);
		char msgType = (char) buffer.get();
		this.newsBean.setMsgType(Character.toString(msgType));
		byte[] bytes = new byte[2];
		buffer.get(bytes);
		String actionCode = new String(bytes).trim();
		int arrayCount = buffer.getShort();
		int arrayOffset = buffer.getShort();
		int arrayLength = buffer.getShort();
		logger.warning("UNPROCESSED symbol_maintainance_IP" + msgType + "," + actionCode + "," + arrayCount + ","
				+ arrayOffset + "," + arrayLength);
	}

	private void processIpBody(ByteBuffer buffer) {
		int sectionLength = buffer.getShort();
		char tempPerm = (char) buffer.get();
		this.newsBean.setTempPerm(Character.toString(tempPerm));
		char retention = (char) buffer.get();
		this.newsBean.setRetention(Character.toString(retention));
		byte[] bytes = new byte[4];
		buffer.get(bytes);
		String newsSource = new String(bytes).trim();
		this.newsBean.setNewsSource(newsSource);
		bytes = new byte[6];
		buffer.get(bytes);
		String branding = new String(bytes).trim();
		this.newsBean.setBranding(branding);
		bytes = new byte[8];
		buffer.get(bytes);
		String displayDate = new String(bytes).trim();
		this.newsBean.setDisplayDate(displayDate);
		bytes = new byte[6];
		buffer.get(bytes);
		String displayTime = new String(bytes).trim();
		this.newsBean.setDisplayTime(displayTime);
		bytes = new byte[2];
		buffer.get(bytes);
		String hotNewsInd = new String(bytes).trim();
		this.newsBean.setHotInd(hotNewsInd);
		bytes = new byte[4];
		buffer.get(bytes);
		String origSource = new String(bytes).trim();
		this.newsBean.setOrigSource(origSource);
		bytes = new byte[14];
		buffer.get(bytes);
		String accessionNumberStr = new String(bytes).trim();
		long accessionNumber = Long.parseLong(accessionNumberStr);
		this.newsBean.setAccessionNumber(accessionNumber);
		bytes = new byte[11];
		buffer.get(bytes);
		String pageCitation = new String(bytes).trim();
		this.newsBean.setPageCitation(pageCitation);
		bytes = new byte[8];
		buffer.get(bytes);
		String gmtDate = new String(bytes).trim();
		this.newsBean.setGmtDate(gmtDate);
		bytes = new byte[6];
		buffer.get(bytes);
		String gmtTime = new String(bytes).trim();
		this.newsBean.setGmtTime(gmtTime);
		bytes = new byte[4];
		buffer.get(bytes);
		String miliSeconds = new String(bytes).trim();
		this.newsBean.setMiliSeconds(miliSeconds);
		int classifyLength = buffer.getShort();
		bytes = new byte[2];
		buffer.get(bytes);
		String classifyEncodingScheme = new String(bytes).trim();
		int remainingCount = buffer.remaining();
		if (remainingCount + 4 != classifyLength)
			logger.warning("MISMATCH Buffer data length " + remainingCount + "," + classifyLength);
		bytes = new byte[remainingCount];
		buffer.get(bytes);
		int index = 0;
		int prvIndex = index;
		while (index < bytes.length) {
			if (bytes[index] != 0x1C) {
				index++;
			} else {
				ArrayList<String> list = new ArrayList<>();
				if (index == prvIndex)
					this.newsBean.getCategories().add(list);
				else {
					String category = new String(bytes, prvIndex, index - prvIndex);
					StringTokenizer st = new StringTokenizer(category);
					while (st.hasMoreTokens()) {
						list.add(st.nextToken());
					}
					this.newsBean.getCategories().add(list);
				}
				index++;
				prvIndex = index;
			}
		}
	}

	private String formatDate(NewsBean newsBean) {
		String date = newsBean.getDisplayDate().trim() + newsBean.getDisplayTime().trim();
		try {
			LocalDateTime datetime = LocalDateTime.parse(date, this.oldPattern);
			return datetime.format(this.newPattern);
		} catch (Exception e) {
			logger.log(Level.WARNING, this.getName() + " " + e.getMessage(), e);
			return null;
		}
	}

	private void saveStory(NewsBean newsBean) {
		this.newsLinkMap.put(newsBean.getNewsId(), newsBean.getNewsLink());
		this.accessionMap.put(newsBean.getAccessionNumber(), newsBean.getNewsLink());
		String filename = "DN" + newsBean.getNewsId() + ".HTML";
		try {
			File newPath = null;
			newPath = new File(this.newsDirectory, filename);
			if (newPath.exists())
				newPath.delete();
			String formattedDate = formatDate(newsBean);
			StringBuilder fullHeadline = new StringBuilder();
			fullHeadline.append("<a name=" + newsBean.getNewsId() + "></a>");
			fullHeadline.append(formattedDate + ", ");
			fullHeadline.append(newsBean.getHeadline());
			String htmlFile = null;
			htmlFile = this.htmlNewsWriter.createHTMLStoryFile(fullHeadline.toString(), newsBean.getBody(),
					Constants.mSource, newPath.getPath());
			String newsLink = null;
			if (htmlFile != null && !htmlFile.isEmpty()) {
				String htmlFileName = htmlFile.substring(htmlFile.lastIndexOf('/'));
				newsLink = this.newsDnsHeader + "/" + htmlFileName.substring(1);
			}
			if (newsLink == null || newsLink.isEmpty()) {
				String vMsg = "DJParser.saveStory(): unable to send News HTML file from file " + filename + ".";
				logger.warning(vMsg);
				newsBean.setNewsLink("noLink");
				return;
			}
			newsBean.setNewsLink(newsLink);
			logger.info("saveStory " + newsLink + " " + filename + " " + htmlFile);
		} catch (Exception e) {
			logger.log(Level.WARNING, this.getName() + " " + e.getMessage(), e);
		}
	}

	private void processFileUpdation(NewsBean newsBean) {
		if (newsBean.getNewsId() == null)
			return;
		if (newsBean.getNewsLink() == null)
			return;
		FileReader fr = null;
		FileWriter fw = null;
		try {
			String url = newsBean.getNewsLink();
			String filename = "";
			int urlIndex = url.indexOf(this.newsPrefix);
			if (urlIndex > 0)
				filename = url.substring(urlIndex + this.newsPrefix.length());
			else
				return;
			filename = this.newsDirectory + "/" + filename;
			fr = new FileReader(filename);
			StringBuilder sb = new StringBuilder();
			char[] data = new char[1024];
			int bytesRead = 0;
			while ((bytesRead = fr.read(data)) >= 0) {
				sb.append(new String(data, 0, bytesRead));
			}
			String currentStory = sb.toString();
			String msgStartTag = "<a name=" + newsBean.getNewsId() + "></a>";
			int newsStartIndex = currentStory.indexOf(msgStartTag);
			boolean headlineChanged = false;
			if (newsStartIndex > 0) {
				sb = new StringBuilder();
				String msgType = newsBean.getMsgType();
				String headline = newsBean.getHeadline();
				if ((msgType.contains("H") || msgType.contains("N")) && headline != null) {
					int headlineStartIndex = currentStory.indexOf(",", newsStartIndex);
					int headlineEndIndex = currentStory.indexOf("</news_headline>", newsStartIndex);
					if (headlineStartIndex == -1 || headlineEndIndex == -1) {
						return;
					}
					sb.append(currentStory.substring(0, headlineStartIndex + 2));
					sb.append(headline);
					sb.append(currentStory.substring(headlineEndIndex));
					headlineChanged = true;
				}
				String body = newsBean.getBody();
				if ((msgType.contains("B") || msgType.contains("N")) && body != null) {
					if (headlineChanged) {
						currentStory = sb.toString();
						sb = new StringBuilder();
					}
					int bodyStartIndex = currentStory.indexOf("<news_story><p>", newsStartIndex);
					bodyStartIndex += "<news_story><p>".length();
					int bodyEndIndex = currentStory.indexOf("</p></news_story>", newsStartIndex);
					if (bodyStartIndex == -1 || bodyEndIndex == -1) {
						return;
					}
					sb.append(currentStory.substring(0, bodyStartIndex));
					sb.append(body);
					sb.append(currentStory.substring(bodyEndIndex));
				}
			}
			currentStory = sb.toString();
			fw = new FileWriter(filename, false);
			fw.write(currentStory, 0, currentStory.length());
			logger.info("processFileUpdation " + newsBean.getNewsLink() + " " + filename + " " + newsBean.getNewsId());
		} catch (Exception e) {
			logger.log(Level.WARNING, this.getName() + " " + e.getMessage(), e);
		} finally {
			if (fr != null)
				try {
					fr.close();
				} catch (Exception frex) {
				}
			if (fw != null)
				try {
					fw.close();
				} catch (Exception fwex) {
				}
		}
	}

	private void processFileMerge(NewsBean newsBean) {
		if (newsBean.getNewsId() == null)
			return;
		if (newsBean.getNewsLink() == null)
			return;
		FileReader fr = null;
		FileWriter fw = null;
		try {
			String url = newsBean.getNewsLink();
			String filename = "";
			int urlIndex = url.indexOf(this.newsPrefix);
			if (urlIndex > 0) {
				filename = url.substring(urlIndex + this.newsPrefix.length());
			} else {
				saveStory(newsBean);
				return;
			}
			filename = this.newsDirectory + "/" + filename;
			fr = new FileReader(filename);
			StringBuilder sb = new StringBuilder();
			char[] data = new char[1024];
			int bytesRead = 0;
			while ((bytesRead = fr.read(data)) >= 0) {
				sb.append(new String(data, 0, bytesRead));
			}
			String currentStory = sb.toString();
			String newsEndTag = "</news_story>";
			int newsEnd = currentStory.lastIndexOf(newsEndTag);
			if (newsEnd == -1) {
				return;
			}
			newsEnd += newsEndTag.length();
			sb = new StringBuilder();
			sb.append(currentStory.substring(0, newsEnd));
			sb.append(
					"</font></td></tr><tr><td><font size=4 style='FONT-FACE:arial,helvetica,sans-serif; FONT-SIZE: 12px;'>"
							+ "<news_headline>");
			StringBuffer fullHeadline = new StringBuffer();
			fullHeadline.append("<a name=" + newsBean.getNewsId() + "></a>");
			fullHeadline.append(formatDate(newsBean) + ", ");
			fullHeadline.append(newsBean.getHeadline());
			sb.append(fullHeadline);
			sb.append(
					"</news_headline></font><font size=2 style='FONT-FACE:arial,helvetica,sans-serif; FONT-SIZE: 11px;' >"
							+ "<news_story><p>");
			sb.append(newsBean.getBody());
			sb.append("</p></news_story>");
			sb.append(currentStory.substring(newsEnd, currentStory.length()));
			currentStory = sb.toString();
			fw = new FileWriter(filename, false);
			fw.write(currentStory, 0, currentStory.length());
			logger.info("processFileMerge " + newsBean.getNewsLink() + " " + filename + " " + newsBean.getNewsId());
		} catch (Exception e) {
			logger.log(Level.WARNING, this.getName() + " " + e.getMessage(), e);
		} finally {
			if (fr != null)
				try {
					fr.close();
				} catch (Exception frex) {
				}
			if (fw != null)
				try {
					fw.close();
				} catch (Exception fwex) {
				}
		}
	}

	private void processFileDelete(NewsBean newsBean) {
		if (newsBean.getNewsId() == null)
			return;
		if (newsBean.getNewsLink() == null)
			return;
		FileReader fr = null;
		FileWriter fw = null;
		try {
			String url = newsBean.getNewsLink();
			String filename = "";
			int urlIndex = url.indexOf(this.newsPrefix);
			if (urlIndex > 0) {
				filename = url.substring(urlIndex + this.newsPrefix.length());
			} else {
				// saveStory();
				return;
			}
			filename = this.newsDirectory + "/" + filename;
			fr = new FileReader(filename);
			StringBuilder sb = new StringBuilder();

			char[] data = new char[1024];

			int bytesRead = 0;
			while ((bytesRead = fr.read(data)) >= 0) {
				sb.append(new String(data, 0, bytesRead));
			}
			String currentStory = sb.toString();
			String newsStartTag = "<news_headline><a name=" + newsBean.getNewsId() + "></a>";
			int newsStartIndex = currentStory.indexOf(newsStartTag);
			if (newsStartIndex == -1) {
				return;
			}
			String newsEndTag = "</news_story>";
			int newsEnd = currentStory.substring(newsStartIndex).indexOf(newsEndTag);
			if (newsEnd == -1) {
				return;
			}
			newsEnd += newsEndTag.length();
			sb = new StringBuilder();
			sb.append(currentStory.substring(0, newsStartIndex));
			sb.append(currentStory.substring(newsEnd, currentStory.length()));
			currentStory = sb.toString();
			fw = new FileWriter(filename, false);
			fw.write(currentStory, 0, currentStory.length());
			logger.info("processFileDelete " + newsBean.getNewsLink() + " " + filename + " " + newsBean.getNewsId());
		} catch (Exception e) {
			logger.log(Level.WARNING, this.getName() + " " + e.getMessage(), e);
		} finally {
			if (fr != null)
				try {
					fr.close();
				} catch (Exception frex) {
				}
			if (fw != null)
				try {
					fw.close();
				} catch (Exception fwex) {
				}
		}
	}

	private boolean checkNewsCategories(NewsBean newsBean) {
		List<String> subject = newsBean.getCategories().get(4);
		if (newsBean.getProduct().equals("DN")) {
			if (subject.contains("N/DJN") && !subject.contains("P/FXEX") && !subject.contains(" P/FXTR")) {
				newsBean.setPublisher("DJN");
				return true;
			} else {
				logger.warning("checkNewsCategories: DN subject not matched " + subject.toString());
				return false;
			}
		} else if (newsBean.getProduct().equals("SN")) {
			if (subject.contains("N/BW")) {
				newsBean.setPublisher("BW");
				return true;
			} else if (subject.contains("N/PR")) {
				newsBean.setPublisher("PR");
				return true;
			} else {
				logger.warning("checkNewsCategories: SN subject not matched " + subject.toString());
				return false;
			}
		} else {
			logger.warning(
					"checkNewsCategories: product not matched " + newsBean.getProduct() + " " + subject.toString());
		}
		return true;
	}

	public void finish() {
		this.doRun = false;
	}
}
