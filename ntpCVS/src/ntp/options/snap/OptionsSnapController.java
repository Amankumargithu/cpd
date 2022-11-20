package ntp.options.snap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import ntp.logger.NTPLogger;
import ntp.options.cache.OptionsQTMessageQueue;
import ntp.util.CPDProperty;
import ntp.util.NTPConstants;
import ntp.util.OptionsUtility;
import QuoddFeed.msg.BlobTable;
import QuoddFeed.msg.Image;
import QuoddFeed.msg.QuoddMsg;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;

public class OptionsSnapController {

	private UltraChan[] channelArray;
	private UltraChan[] preChannelArray;
	private static OptionsSnapController instance = new OptionsSnapController();
	private long count = 0;
	private String[] flds = {};
	private Object obj = new Object();
	private OptionsUtility utility = OptionsUtility.getInstance();

	public static OptionsSnapController getInstance() {
		return instance;
	}

	private OptionsSnapController() {
		int snapChannelCount = 5;
		String preIP = null;
		int prePort = 4321;
		try {
			snapChannelCount = Integer.parseInt(CPDProperty.getInstance().getProperty("NUMBER_OF_SNAP_CHANNELS"));
		} catch (Exception e) {
			NTPLogger.missingProperty("NUMBER_OF_SNAP_CHANNELS");
			snapChannelCount = 5;
			NTPLogger.defaultSetting("NUMBER_OF_SNAP_CHANNELS", "" + snapChannelCount);
		}
		String ip = CPDProperty.getInstance().getProperty("SNAP_UC_IP");
		if (ip == null) {
			NTPLogger.missingProperty("SNAP_UC_IP");
			ip = NTPConstants.IP;
			NTPLogger.defaultSetting("SNAP_UC_IP", ip);
		}
		int port = NTPConstants.PORT;
		try {
			port = Integer.parseInt(CPDProperty.getInstance().getProperty("SNAP_UC_PORT"));
		} catch (Exception e) {
			NTPLogger.missingProperty("SNAP_UC_PORT");
			port = NTPConstants.PORT;
			NTPLogger.defaultSetting("SNAP_UC_PORT", "" + port);
		}
		try {
			preIP = CPDProperty.getInstance().getProperty("PRE_SNAP_IP");
		} catch (Exception e) {
			NTPLogger.missingProperty("PRE_SNAP_IP");
			preIP = "192.168.192.170";
			NTPLogger.defaultSetting("PRE_SNAP_IP", preIP);
		}
		try {
			prePort = Integer.parseInt(CPDProperty.getInstance().getProperty("PRE_SNAP_PORT"));
		} catch (Exception e) {
			NTPLogger.missingProperty("PRE_SNAP_PORT");
			prePort = 4321;
			NTPLogger.defaultSetting("PRE_SNAP_PORT", "" + prePort);
		}
		channelArray = new UltraChan[snapChannelCount];
		preChannelArray = new UltraChan[snapChannelCount];
		for (int i = 0; i < snapChannelCount; i++) {
			String name = "OpSnap_" + i;
			UltraChan chan = new UltraChan(ip, port, name, "password", false);
			channelArray[i] = chan;
			chan.Start();
			NTPLogger.connectChannel(name, NTPConstants.IP);
		}
		for (int i = 0; i < snapChannelCount; i++) {
			String name = "PreOpSnap_" + i;
			UltraChan chan = new UltraChan(preIP, prePort, name, "password", false);
			preChannelArray[i] = chan;
			chan.Start();
			NTPLogger.connectChannel(name, NTPConstants.IP);
		}
	}

	private synchronized UltraChan getChannel(boolean isPre) {
		++count;
		int index = (int) (count % channelArray.length);
		if (isPre)
			return preChannelArray[index];
		else
			return channelArray[index];
	}

	public BlobTable getOptionChain(String tkr) {
		UltraChan channel = getChannel(false);
		BlobTable bt = null;
		int resubscribeCount = -1;
		do {
			NTPLogger.requestUC("SyncGetOptionSnap", tkr, ++resubscribeCount);
			bt = channel.SyncGetOptionSnap(tkr, obj, flds);
			NTPLogger.responseUC("SyncGetOptionSnap", tkr, bt == null ? 0 : bt.nRow());
		} while (bt != null && bt.len() == 0 && resubscribeCount < 2);
		if (bt != null && bt.len() == 0 && resubscribeCount == 2)
			NTPLogger.syncAPIOverrun("SyncGetOptionSnap", resubscribeCount);
		return bt;
	}

	public BlobTable getPreOptionChain(String tkr) {
		UltraChan channel = getChannel(true);
		BlobTable bt = null;
		int resubscribeCount = -1;
		do {
			NTPLogger.requestUC("SyncGetOptionSnap", tkr, ++resubscribeCount);
			bt = channel.SyncGetOptionSnap(tkr, obj, flds);
			NTPLogger.responseUC("SyncGetOptionSnap", tkr, bt == null ? 0 : bt.nRow());
		} while (bt != null && bt.len() == 0 && resubscribeCount < 2);
		if (bt != null && bt.len() == 0 && resubscribeCount == 2)
			NTPLogger.syncAPIOverrun("SyncGetOptionSnap", resubscribeCount);
		return bt;
	}

	public Image getOptionsFundamentalData(String ticker) {
		UltraChan channel = getChannel(false);
		try {
			boolean rerunFlag = false;
			int resubscribeCount = -1;
			do {
				NTPLogger.requestUC("SyncSnap", ticker, ++resubscribeCount);
				QuoddMsg tickerSnap = channel.SyncSnap(ticker, obj);
				NTPLogger.responseUC("SyncSnap", ticker, tickerSnap == null ? 0 : 1);
				char mt = tickerSnap.mt();
				if (tickerSnap instanceof Image) {
					rerunFlag = false;
					Image img = (Image) tickerSnap;
					img.SetTkr(ticker);
					return img;
				} else if (mt == UltraChan._mtDEAD) {
					String tkr = null;
					Status sts = (Status) tickerSnap;
					try {
						tkr = sts.tkr().substring(0, sts.tkr().indexOf("\""));
					} catch (Exception e) {
						tkr = sts.tkr();
					}
					if (sts.IsIOException())
						rerunFlag = true;
					else {
						NTPLogger.dead(tkr, "OpSnap");
						if (tkr != null)
							OptionsQTMessageQueue.getInstance().getIncorrectTickerMap().put(tkr, tkr);
						rerunFlag = false;
					}
				} else
					NTPLogger.unknown(ticker, "OpSnap", mt);
			} while (rerunFlag && resubscribeCount < 2);
			if (rerunFlag && resubscribeCount == 2)
				NTPLogger.syncAPIOverrun("SyncSnap", resubscribeCount);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public HashMap<String, Image> getSyncSnapData(HashSet<String> resubscribeSet, boolean isPre) {
		HashMap<String, Image> map = new HashMap<>();
		int resubscribeCount = -1;
		UltraChan channel = getChannel(isPre);
		if (isPre) {
			HashSet<String> tempSet = new HashSet<>();
			tempSet.addAll(resubscribeSet);
			resubscribeSet.clear();
			for (String ticker : tempSet) {
				ticker = ticker.replace("O:", "P:");
				resubscribeSet.add(ticker);
			}
		}
		try {
			do {
				QuoddMsg[] tickerSnaps = null;
				String[] tkrArr = resubscribeSet.toArray(new String[0]);
				resubscribeSet.clear();
				int tickCount = tkrArr.length;
				for (int index = 0; index < tickCount; index += 1000) {
					int destRange = Math.min(index + 1000, tickCount);
					Object obj = new Object();
					NTPLogger.requestUC("SyncMultiSnap", (destRange - index) + " ticker", ++resubscribeCount);
					tickerSnaps = channel.SyncMultiSnap(Arrays.copyOfRange(tkrArr, index, destRange), obj);
					NTPLogger.responseUC("SyncMultiSnap", (destRange - index) + " ticker",
							tickerSnaps == null ? 0 : tickerSnaps.length);
					for (int j = 0; j < tickerSnaps.length; j++) {
						char mt = tickerSnaps[j].mt();
						if (tickerSnaps[j] instanceof Image) {
							Image rtn = (Image) tickerSnaps[j];
							String ticker = rtn.tkr();
							int quoteIndex = ticker.indexOf("\"");
							if (quoteIndex != -1)
								ticker = ticker.substring(0, quoteIndex);
							if (isPre) {
								ticker = ticker.replace("P:", "O:");
								rtn.SetTkr(ticker);
							}
							ticker = utility.getEQPlusFormattedTicker(ticker);
							map.put(ticker, rtn);
						} else if (mt == UltraChan._mtDEAD) {
							String tkr = null;
							Status sts = (Status) tickerSnaps[j];
							try {
								tkr = sts.tkr().substring(0, sts.tkr().indexOf("\""));
							} catch (Exception e) {
								tkr = sts.tkr();
							}
							if (sts.IsIOException())
								resubscribeSet.add(tkr);
							else {
								NTPLogger.dead(tkr, "OpSyncMulti");
								if (!isPre)
									OptionsQTMessageQueue.getInstance().getIncorrectTickerMap().put(tkr, tkr);
							}
						} else
							NTPLogger.unknown(tickerSnaps[j].tkr(), "OpSyncMulti", mt);
					}
					tickerSnaps = null;
				}
			} while (!resubscribeSet.isEmpty() && resubscribeCount < 2);
			if (!resubscribeSet.isEmpty() && resubscribeCount == 2)
				NTPLogger.syncAPIOverrun("SyncMultiSnap", resubscribeCount);
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error ee) {
			ee.printStackTrace();
		}
		return map;
	}

}
