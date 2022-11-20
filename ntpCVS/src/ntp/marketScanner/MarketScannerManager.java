package ntp.marketScanner;

import java.util.Collection;

public class MarketScannerManager {

	private int numberOfChannels = 6;
	private int channelCount = 0;
	private MarketScannerChannel[] channelArray = null;

	public MarketScannerManager(int channels) {
		this.numberOfChannels = channels;
		channelArray = new MarketScannerChannel[channels];
		for (int i = 0; i < numberOfChannels; i++) {
			MarketScannerChannel channel = new MarketScannerChannel("ScanChan " + (i + 1));
			channelArray[i] = channel;
		}
	}

	public void subscribeTickers(Collection<String> tickerList) {
		MarketScannerChannel channel = channelArray[channelCount];
		channelCount++;
		channel.subscribeTickers(tickerList);
	}
}
