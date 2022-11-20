package com.b4utrade.stockutil;

import com.b4utrade.helper.*;

import java.text.SimpleDateFormat;
import java.util.*;

import com.tacpoint.util.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

public class MarketMakerOrderManager extends DefaultStockObject {
	
	
    private transient static String tickerString = "ticker=";
    private transient static String restrictedString = "restricted=";
    private transient static String bidsBeginString = "bids=";
    private transient static String bidsEndString = "endbids";
    private transient static String asksBeginString = "asks=";
    private transient static String asksEndString = "endasks";
    private transient static String mmpDelim = "||";
    private transient static String mmpElementDelim = ",";
    
    public transient static String MM2_TICKER_APPENDER = ".MM2";

    static transient SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    
	private String ticker = null;
	
	//private TreeSet bids = new TreeSet(new MarketMakerBidComparator());
	//private TreeSet asks = new TreeSet(new MarketMakerAskComparator());

	
	private List bids = new ArrayList();
	private List asks = new ArrayList();
	

	private boolean restricted = false;

	private int maxObjects = 15;
	
	private static final String ZERO = "0";
	
	private char[] serializedBidList;
	private char[] serializedAskList;

	private MarketMakerStockTopLists list = new MarketMakerStockTopLists();
	
	private MarketMakerBidComparator bidComparator = new MarketMakerBidComparator();
	private MarketMakerAskComparator askComparator = new MarketMakerAskComparator();
	
	private  HashMap TOP_OF_THE_BOOK_EXCLUSIONS = new HashMap();
	
	public boolean isRegionalMarketMaker(String mmid) {
		return TOP_OF_THE_BOOK_EXCLUSIONS.containsKey(mmid);
	}
	
	private void init() {
		TOP_OF_THE_BOOK_EXCLUSIONS.put("FLOW","FLOW");
		TOP_OF_THE_BOOK_EXCLUSIONS.put("ARCA","ARCA");
		TOP_OF_THE_BOOK_EXCLUSIONS.put("AMEX","AMEX");
		TOP_OF_THE_BOOK_EXCLUSIONS.put("BATS","BATS");
		TOP_OF_THE_BOOK_EXCLUSIONS.put("BOSX","BOSX");
		TOP_OF_THE_BOOK_EXCLUSIONS.put("CBSX","CBSX");
		TOP_OF_THE_BOOK_EXCLUSIONS.put("CINN","CINN");
		TOP_OF_THE_BOOK_EXCLUSIONS.put("ISEG","ISEG");
		TOP_OF_THE_BOOK_EXCLUSIONS.put("MWSE","MWSE");
		TOP_OF_THE_BOOK_EXCLUSIONS.put("NDQR","NDQR");
		TOP_OF_THE_BOOK_EXCLUSIONS.put("NYSE","NYSE");
		TOP_OF_THE_BOOK_EXCLUSIONS.put("OMDF","OMDF");
		TOP_OF_THE_BOOK_EXCLUSIONS.put("PHLX","PHLX");	
		TOP_OF_THE_BOOK_EXCLUSIONS.put("BATY","BATY");	
		TOP_OF_THE_BOOK_EXCLUSIONS.put("EDGA","EDGA");	
		TOP_OF_THE_BOOK_EXCLUSIONS.put("EDGX","EDGX");	
	}
	
	public MarketMakerOrderManager() {
		mStockType = StockTypeConstants.MARKETMAKER_TOP_LISTS;
		maxObjects += 5;
		init();
	}

	public MarketMakerOrderManager(int maxObjects) {
		mStockType = StockTypeConstants.MARKETMAKER_TOP_LISTS;
		if (maxObjects > 0)
			this.maxObjects = maxObjects + 5;	
		init();
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
		list.setTicker(ticker);
	}

	public String getTicker() {
		return ticker;
	}

	public void setListsMaxNumber(int inListsMaxNumber) {
		maxObjects = inListsMaxNumber;
	}

	public int getListsMaxNumber() {
		return maxObjects;
	}

	public void setRestricted(boolean inRestricted) {
		restricted = inRestricted;
		list.setRestricted(restricted);
	}

	public boolean getRestricted() {
		return restricted;
	}

	public MarketMakerStockTopLists getMarketMakerStockTopList() {

		list.getTopBidsList().clear();
		list.getTopBidsList().addAll(bids);

		list.getTopAsksList().clear();
		list.getTopAsksList().addAll(asks);

		return list;
	}

	public void updateStock(MarketMakerActivity stock) {
		
	
		if (stock == null)
			return;

		if (ticker == null || ticker.length() == 0) {
			ticker = stock.getTicker();
			list.setTicker(ticker);
		}

		
		/*
		if (stock.mTicker.equals("OCLS.MM2")) {
		    System.out.println(stock.mTicker+" "+stock.mMarketMakerID+" bid price : "+stock.mBid+" ask price : "+stock.mAsk+" top of bid book indicator ? "+stock.topOfTheBidBookIndicator+" top of ask book indicator ? "+stock.topOfTheAskBookIndicator);			
		}
		*/
		
		restricted = stock.getRestricted();
		list.setRestricted(restricted);

		updateTopBidList(stock);
		updateTopAskList(stock);
		
		//System.out.println("Time to process record for ticker : "+stock.mTicker+" "+ (System.currentTimeMillis() - beginTime));
		
		/*
		if (stock.mTicker.equals("OCLS.MM2")) {
			
			MarketMakerPrice topBid = null;
			MarketMakerPrice topAsk = null;
			
			if (bids.size() > 0) {
				topBid = (MarketMakerPrice)bids.get(0);
			}
			if (asks.size() > 0) {
				topAsk = (MarketMakerPrice)asks.get(0);
			}
			
			if (topBid != null)
				System.out.println(stock.mTicker+" - top bid : "+topBid.mMarketMakerID+" "+topBid.getPrice()+" "+topBid.getSize());
			if (topAsk != null)
				System.out.println(stock.mTicker+" - top ask : "+topAsk.mMarketMakerID+" "+topAsk.getPrice()+" "+topAsk.getSize());

		}
		*/

	}

	private void updateTopBidList(MarketMakerActivity mma) {
		if (mma == null)
			return;

		String mmid = mma.getMarketMakerID();
		
		if (mmid == null || mmid.equals(StockItems.NOACTIVITY))
			return;
		
		// no need to process N/A updates ...
		if (mma.mBid.equals(StockItems.NOACTIVITY) || mma.mBidSize.equals(StockItems.NOACTIVITY))
			return;
		
		double bidPrice = getPriceInDecimal(mma.getBid());
		
		/*
		if (bidPrice < 0.01) {
			System.out.println(mma.getTicker()+" original bid price : "+mma.getBid()+ " converted bid price : "+bidPrice);
		}
		*/

		MarketMakerPrice mmp = new MarketMakerPrice();
		mmp.setPrice(mma.getBid());
		mmp.setSize(mma.getBidSize());
		mmp.setMarketMakerID(mmid);
		mmp.setPriceAsDouble(bidPrice);
		mmp.setUPC(mma.getUPC());
		mmp.setTime(mma.getTime());		
		
		
		/*
		//
		// if market maker activity bid price is > than best market maker bid price in our current list, do not process UNLESS it's tagged as top of the book!
		//
		
		if (!mma.topOfTheBidBookIndicator) {			
			MarketMakerPrice topBid = (MarketMakerPrice)bids.get(0);
			if (topBid != null) {
				double bestBidPrice = topBid.getPriceAsDouble();
				if (bidPrice > bestBidPrice) {
					return;
				}
			}			
		}
		*/

		//System.out.println("About to remove market maker : "+mmp.getMarketMakerID()+" where bid price = "+bidPrice + " and bid size = "+mma.mBidSize);
		
		//
		// 12/12/2011 - do not remove MMIDs even if the bid price is zero (per Angie)
		//
		
		
		bids.remove(mmp);	
		
		
		// 12/15/11 - only leave unpriced market makers for .PK stocks!
		if (bidPrice < .00001)  {
			if (!ticker.endsWith(".PK")) {
				serializeBidList();	
				//System.out.println("Discarding unpriced bid market maker for ticker : "+ticker);
				return;					
			}	
			/*
			else {
				System.out.println("Processing unpriced bid market maker for ticker : "+ticker);
			}
			*/
		}

		
		/*
		
		//if (ticker.equals("LNKD") && mma.getMarketMakerID().equals("BATY"))
		//	System.out.println(new Date()+" LNKD - Removing market maker BATY - bid price : "+bidPrice);
	
		if ( (mma.mBidSize != null && mma.mBidSize.equals(ZERO)) || bidPrice <= .00001) {			
			// serialize the bid list and return ...
			serializeBidList();				
			return;
		}
		*/
		
		if (mma.mBidSize != null) {
			try {
				mmp.setSizeAsInt(Integer.parseInt(mma.mBidSize));
			}
			catch (NumberFormatException nfe) {
				// should never happen ...
			}
		}	
		
		/*
		MarketMakerPrice last = (MarketMakerPrice)bids.get(bids.size()-1);
		if (mmp.getPriceAsDouble() < last.getPriceAsDouble() && bids.size() == maxObjects)
			return;
		*/
		
		//System.out.println("About to add market maker : "+mmp.getMarketMakerID()+" where bid price = "+bidPrice + " and bid size = "+mma.mBidSize);
		
		bids.add(mmp);
		
		if (bidPrice > .00001) {
			
			//if (ticker.equals("LNKD") && mma.getMarketMakerID().equals("BATY"))
			//	System.out.println(new Date()+" LNKD - Added market maker BATY - bid price : "+bidPrice);
			
			Collections.sort(bids, bidComparator);	
		
			if (mma.topOfTheBidBookIndicator) {				
				
				try {
					
					/*
					TreeSet tempBidSet =  new TreeSet(new MarketMakerBidComparator());
					
					SortedSet headSet = bids.headSet(mmp);
					
					if (headSet != null) {
						Iterator hsi = headSet.iterator();
						while (hsi.hasNext()) {
							MarketMakerPrice item = (MarketMakerPrice)hsi.next();
							if (TOP_OF_THE_BOOK_EXCLUSIONS.containsKey(item.getMarketMakerID())) {
								tempBidSet.add(item);
							}
						}
					}
					
					tempBidSet.addAll(bids.tailSet(mmp));
					bids = tempBidSet;
					*/	
					
					int index = bids.indexOf(mmp);					
				
					if (index > 0) {					
						
						while (--index >= 0) {
							
							MarketMakerPrice item = (MarketMakerPrice)bids.get(index);
							
							// ignore the market maker if it's a regional market maker ...
							if (TOP_OF_THE_BOOK_EXCLUSIONS.containsKey(item.getMarketMakerID())) {
								continue;
							}
							
							// remove the market maker ...
							bids.remove(index);						
						
						}					
					}
					
							
				}
				catch (Exception e) {
					System.out.println("Error removing top bids - "+e.getMessage());
					e.printStackTrace();
				}
			}
		}

		try {
			while (bids.size() > maxObjects) {
				bids.remove(bids.size() - 1);		
			}
						
		}
		catch (Exception e) {
			System.out.println("Error removing bottom bids - "+e.getMessage());
		}
		
		serializeBidList();		
		
	}


	
	private void updateTopAskList(MarketMakerActivity mma) {
	
		if (mma == null)
			return;

		String mmid = mma.getMarketMakerID();
		
		if (mmid == null || mmid.equals(StockItems.NOACTIVITY))
			return;

		// no need to process N/A updates ...
		if (mma.mAsk.equals(StockItems.NOACTIVITY) || mma.mAskSize.equals(StockItems.NOACTIVITY))
			return;
		
		double askPrice = getPriceInDecimal(mma.getAsk());
		
		/*
		if (askPrice < 0.01) {
			System.out.println(mma.getTicker()+" original ask price : "+mma.getAsk()+ " converted ask price : "+askPrice);
		}
		*/

		MarketMakerPrice mmp = new MarketMakerPrice();
		mmp.setPrice(mma.getAsk());
		mmp.setSize(mma.getAskSize());
		mmp.setMarketMakerID(mmid);
		mmp.setPriceAsDouble(askPrice);
		mmp.setUPC(mma.getUPC());
		mmp.setTime(mma.getTime());			
		
		
		/*
		//
		// if market maker activity ask price is < than best market maker ask price in our current list, do not process UNLESS it's tagged as top of the book!
		//
		
		if (!mma.topOfTheAskBookIndicator) {			
			MarketMakerPrice topAsk = (MarketMakerPrice)asks.get(0);
			if (topAsk != null) {
				double bestAskPrice = topAsk.getPriceAsDouble();
				if (askPrice < bestAskPrice) {
					return;
				}
			}			
		}
		*/
		
		
		//
		// 12/12/2011 - do not remove MMIDs even if the ask price is zero (per Angie)
		//
		
		asks.remove(mmp);
		
		// 12/15/11 - only leave unpriced market makers for .PK stocks!
		if (askPrice < .00001)  {
			if (!ticker.endsWith(".PK")) {
				serializeAskList();	
				//System.out.println("Discarding unpriced ask market maker for ticker : "+ticker);
				return;					
			}	
			/*
			else {
				System.out.println("Processing unpriced ask market maker for ticker : "+ticker);
			}
			*/
		}
		
		
		//if (ticker.equals("LNKD") && mma.getMarketMakerID().equals("BATY"))
		//	System.out.println(new Date()+" LNKD - Removing market maker BATY - ask price : "+askPrice);
		
		/*
		if ( (mma.mAskSize != null && mma.mAskSize.equals(ZERO)) || askPrice <= .00001) {			
			// serialize the bid list and return ...
			serializeAskList();			
			return;
		}
		*/
		
		if (mma.mAskSize != null) {
			try {
				mmp.setSizeAsInt(Integer.parseInt(mma.mAskSize));
			}
			catch (NumberFormatException nfe) {
				// should never happen ...
			}
		}
		/*
		MarketMakerPrice last = (MarketMakerPrice)asks.get(asks.size()-1);
		if (mmp.getPriceAsDouble() > last.getPriceAsDouble() && asks.size() == maxObjects)
			return;
		*/
		
		
		asks.add(mmp);
		
		if (askPrice > .00001) {
			
			//if (ticker.equals("LNKD") && mma.getMarketMakerID().equals("BATY"))
			//	System.out.println(new Date()+" LNKD - Adding market maker BATY - ask price : "+askPrice);
			
			Collections.sort(asks, askComparator);
			

			if (mma.topOfTheAskBookIndicator) {		
				
				/*
				TreeSet tempAskSet =  new TreeSet(new MarketMakerAskComparator());
				
				SortedSet headSet = asks.headSet(mmp);
				
				if (headSet != null) {
					Iterator hsi = headSet.iterator();
					while (hsi.hasNext()) {
						MarketMakerPrice item = (MarketMakerPrice)hsi.next();
						if (TOP_OF_THE_BOOK_EXCLUSIONS.containsKey(item.getMarketMakerID())) {
							tempAskSet.add(item);
						}
					}
				}
				
				tempAskSet.addAll(asks.tailSet(mmp));
				asks = tempAskSet;
				*/
				
				
		
				int index = asks.indexOf(mmp);					
				
				if (index > 0) {					
					
					while (--index >= 0) {
						
						MarketMakerPrice item = (MarketMakerPrice)asks.get(index);
						
						// ignore the market maker if it's a regional market maker ...
						if (TOP_OF_THE_BOOK_EXCLUSIONS.containsKey(item.getMarketMakerID())) {
							continue;
						}
						
						// remove the market maker ...
						asks.remove(index);						
					
					}					
				}
				
			}
		}
	
		try {
			while (asks.size() > maxObjects) {		    
				asks.remove(asks.size() - 1);		
			}
		}
		catch (Exception e) {
			System.out.println("Error removing end asks - "+e.getMessage());
		}
		
		serializeAskList();		
	}

	private double getPriceInDecimal(String price) {

		if (price == null || price.length() == 0)
			return 0.0;

		double decimalPrice = 0.0;

		try {

			//if (price.indexOf('.') >= 0)
				decimalPrice = Double.parseDouble(price);
			//else
			//	decimalPrice = Fraction.convertToDecimal(price);

			/*
			BigDecimal bigDecimal = new BigDecimal(decimalPrice);
			bigDecimal = bigDecimal.setScale(5, BigDecimal.ROUND_HALF_UP);
			decimalPrice = bigDecimal.doubleValue();
			*/
		} catch (Exception e) {
			decimalPrice = 0.0d;
		}

		return decimalPrice;
	}
	

	
	public byte[] convertToByteArray() {
		
    	StringBuilder sb = new StringBuilder(500);

        sb.append(tickerString);
        sb.append(ticker+MM2_TICKER_APPENDER);

        //append restricted
        sb.append(restrictedString);
        sb.append(restricted);

        //append bids
        if (serializedBidList != null)
        	sb.append(serializedBidList);

        //append asks
        if (serializedAskList != null)
        	sb.append(serializedAskList);        
        
        char[] chars = new char[sb.length()];
        sb.getChars(0,chars.length,(char[])chars,0);        
        
        byte[] baos = new byte[chars.length];

        // can only do this since we know all chars are ascii!
        for (int i=0; i<chars.length; i++)
           baos[i] = (byte)chars[i];

        return baos;
        
	}
	
	private void serializeBidList() {
		
		StringBuilder sb = new StringBuilder(200);
		sb.append(bidsBeginString);
		
        Iterator it = bids.iterator();
        while (it.hasNext()) {
            sb.append(mmpDelim);
            MarketMakerPrice mmp = (MarketMakerPrice)it.next();
            sb.append(mmp.getUPC());
            sb.append(mmpElementDelim);
            sb.append(mmp.getMarketMakerID());
            sb.append(mmpElementDelim);
            sb.append(mmp.getPrice());
            sb.append(mmpElementDelim);
            sb.append(mmp.getSize());
            sb.append(mmpElementDelim);
            mmp.formatTime(sdf);
            sb.append(mmp.getTimeInString());
        }
        
        sb.append(bidsEndString);
        
        serializedBidList = new char[sb.length()];
        sb.getChars(0,serializedBidList.length,serializedBidList,0);
        
	}
	
	private void serializeAskList() {
		
		StringBuilder sb = new StringBuilder(200);
		sb.append(asksBeginString);
		
        Iterator it = asks.iterator();
        while (it.hasNext()) {
            sb.append(mmpDelim);
            MarketMakerPrice mmp = (MarketMakerPrice)it.next();
            sb.append(mmp.getUPC());
            sb.append(mmpElementDelim);
            sb.append(mmp.getMarketMakerID());
            sb.append(mmpElementDelim);
            sb.append(mmp.getPrice());
            sb.append(mmpElementDelim);
            sb.append(mmp.getSize());
            sb.append(mmpElementDelim);
            mmp.formatTime(sdf);
            sb.append(mmp.getTimeInString());
        }
        
        sb.append(asksEndString);
        
        serializedAskList = new char[sb.length()];
        sb.getChars(0,serializedAskList.length,serializedAskList,0);
        
	}
	

}


