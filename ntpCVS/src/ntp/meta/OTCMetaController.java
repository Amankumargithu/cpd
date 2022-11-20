package ntp.meta;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import ntp.bean.EquityMetaBean;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;

public class OTCMetaController {

	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

	public static void main(String[] args) {
		NTPLogger.info("Process started");
		try
		{
			//Files needed - exchangeMapping, mcloc, symbols_equity, meta_equity
			//Files to generate - daily file of complete meta data

			//Starting with reading and determining properties
			CPDProperty prop = CPDProperty.getInstance();
			Date date = new Date();
			String parseDate = prop.getProperty("PARSE_DATE");
			if(parseDate != null)
			{
				date = formatter.parse(parseDate);
			}
			parseDate = formatter.format(date);
			NTPLogger.info("Parsing Meta data for date " + parseDate);
			String rootDir = prop.getProperty("ROOT_DIRECTORY");
			if(rootDir == null)
			{
				NTPLogger.missingProperty("ROOT_DIRECTORY");
				rootDir = "/home/eqMeta";
				NTPLogger.defaultSetting("ROOT_DIRECTORY", rootDir);
			}
			//This property should not be set as general
			String exchangeMappingFile = prop.getProperty("EXCHANGE_MAPPING_FILE");
			if(exchangeMappingFile == null)
			{
				NTPLogger.missingProperty("EXCHANGE_MAPPING_FILE");
				exchangeMappingFile = "equityExchangeMapping.csv";
				NTPLogger.defaultSetting("EXCHANGE_MAPPING_FILE", exchangeMappingFile);
			}
			//This property should not be set as general
			String marketCenterLocateFile = prop.getProperty("MC_LOCATE_FILE");
			if(marketCenterLocateFile == null)
			{
				NTPLogger.missingProperty("MC_LOCATE_FILE");
				marketCenterLocateFile = "mcloc_tsx.txt";
				NTPLogger.defaultSetting("MC_LOCATE_FILE", marketCenterLocateFile);
			}
			//This property should not be set as general
			String metaDataFile = prop.getProperty("METADATA_FILE");
			if(metaDataFile == null)
			{
				NTPLogger.missingProperty("METADATA_FILE");
				metaDataFile = "meta_tsx.txt";
				NTPLogger.defaultSetting("METADATA_FILE", metaDataFile);
			}
			String outputMetaFile = prop.getProperty("OUTPUT_META_FILE");
			if(outputMetaFile == null)
			{
				NTPLogger.missingProperty("OUTPUT_META_FILE");
				outputMetaFile = "outputMetaTSX.csv";
				NTPLogger.defaultSetting("OUTPUT_META_FILE", outputMetaFile);
			}
			String outputMclocFile = prop.getProperty("OUTPUT_MCLOC_FILE");
			if(outputMclocFile == null)
			{
				NTPLogger.missingProperty("OUTPUT_MCLOC_FILE");
				outputMclocFile = "outputMclocTSX.csv";
				NTPLogger.defaultSetting("OUTPUT_MCLOC_FILE", outputMclocFile);
			}
			String outputPrimaryMCFile = prop.getProperty("OUTPUT_PRIMARY_MC_FILE");
			if(outputPrimaryMCFile == null)
			{
				NTPLogger.missingProperty("OUTPUT_PRIMARY_MC_FILE");
				outputPrimaryMCFile = "outputPrimaryMcTSX.csv";
				NTPLogger.defaultSetting("OUTPUT_PRIMARY_MC_FILE", outputPrimaryMCFile);
			}

			Hashtable<String, String> exchangeMap = new Hashtable<>();
			Hashtable<String, String> locateCodeMap = new Hashtable<>();
			HashMap<String, EquityMetaBean> beanMap = new HashMap<>();
			HashMap<String, String> primaryMarketMap = new HashMap<>();


			String fileName = rootDir + "/" + exchangeMappingFile;
			NTPLogger.info("Reading file " + fileName);
			CsvReader fileReader = new CsvReader(fileName);
			if(fileReader != null)
			{
				while(fileReader.readRecord())
					exchangeMap.put(fileReader.get(0), fileReader.get(1));
			}
			fileReader.close();
			fileName = rootDir + "/" + parseDate + "/" + marketCenterLocateFile;
			NTPLogger.info("Reading file " + fileName);
			BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
			if(bufferedReader != null)
			{
				String line = null;
				while( (line = bufferedReader.readLine()) != null)
				{
					String[] arr = line.split(" ");
					String [] values = arr[3].split(",");
					locateCodeMap.put(values[1], values[2]);
				}				
			}
			bufferedReader.close();
			fileName = rootDir + "/" + parseDate + "/" + outputMclocFile; ;
			NTPLogger.info("Writing file " + fileName);
			CsvWriter fileWriter = new CsvWriter(fileName);
			for(String key : locateCodeMap.keySet())
			{
				fileWriter.write(locateCodeMap.get(key));
				fileWriter.write(exchangeMap.get(locateCodeMap.get(key)));
				fileWriter.endRecord();
//				System.out.println("MC_LOCATE: " + key + " " + locateCodeMap.get(key) + " " + exchangeMap.get(locateCodeMap.get(key)));
			}
			fileWriter.close();
			fileName = rootDir + "/" + parseDate + "/" + metaDataFile;
			NTPLogger.info("Reading file " + fileName);
			bufferedReader = new BufferedReader(new FileReader(fileName));
			if(bufferedReader != null)
			{
				String line = null;
				while( (line = bufferedReader.readLine()) != null)
				{

					String[] arr = line.split(" ");
					String [] values = arr[4].split(",");
					String ticker = arr[6].trim().substring(0, arr[6].length()-1);
					String protocol = values[1].substring(0,2);
					EquityMetaBean bean = beanMap.get(ticker + "_" + protocol);
					if(bean == null)
					{
						bean = new EquityMetaBean();
						bean.setTicker(ticker);
						bean.setProtocol(protocol);
						beanMap.put(ticker + "_" + protocol, bean);
					}
					String[] meta = arr[7].split("=");
					try
					{
						switch (meta[0]) {
						case "1":
							bean.setDescription(line.substring(line.indexOf(arr[7])).substring(2));
							break;
						case "2":
							bean.setSourceSymbol(meta[1]);
							break;
						case "3":
							String prmMkt = arr[9];
							String exchange = locateCodeMap.get(prmMkt);
							if(exchange != null)
								bean.setPrimaryMarket(exchangeMap.get(exchange));
							primaryMarketMap.put(ticker, bean.getPrimaryMarket());
							break;
						case "4":
							bean.setRoundLotSize(arr[9]);
							break;
						case "5":
							if(meta[1].startsWith("<"))
								bean.setIssueType("" + Integer.parseInt(meta[1].substring(1,2), 16));
							else
								bean.setIssueType(meta[1]);
							break;
						case "6":
							if(meta[1].startsWith("<"))
							bean.setFinancialStatus("" + Integer.parseInt(meta[1].substring(1,2), 16));
							else
								bean.setFinancialStatus(meta[1]);
							break;
						case "7":
							bean.setTier("" + Integer.parseInt(meta[1].substring(1,2), 16));
							break;
						case "8":
							bean.setDisclosureStatus("" + Integer.parseInt(meta[1].substring(1,2), 16));
							break;
						case "9":
							bean.setAssetClass("" + Integer.parseInt(meta[1].substring(1,2), 16));
							break;
						case "10":
							bean.setAuthenticity("" + Integer.parseInt(meta[1].substring(1,2), 16));
							break;
						case "11":
							if(meta[1].equals("<00>"))
								bean.setShortSaleRestricted("0");
							else
								bean.setShortSaleRestricted("1");
							break;
						case "12":
							if(meta[1].equals("<00>"))
								bean.setCaveatEmptor("0");
							else
								bean.setCaveatEmptor("1");
							break;
						case "13":
							bean.setClassification("" + Integer.parseInt(meta[1].substring(1,2), 16));
							break;
						case "14":
							bean.setMarketCategory("" + Integer.parseInt(meta[1].substring(1,2), 16));
							break;
						case "15":
							bean.setCurrency(meta[1]);
							break;
						case "16":
							bean.setCusip(meta[1]);
							break;
						case "17":
							if(meta[1].equals("<00>"))
								bean.setPiggybackExempt("0");
							else
								bean.setPiggybackExempt("1");
							break;
						case "18":
							if(meta[1].equals("<00>"))
								bean.setRegSho("0");
							else
								bean.setRegSho("1");
							break;
						case "19":
							if(meta[1].equals("<00>"))
								bean.setUnsolicitedOnly("0");
							else
								bean.setUnsolicitedOnly("1");
							break;
						case "20":
							if(meta[1].equals("<00>"))
								bean.setBbQuoted("0");
							else
								bean.setBbQuoted("1");
							break;
						case "21":
							if(meta[1].equals("<00>"))
								bean.setMessagingDisabled("0");
							else
								bean.setMessagingDisabled("1");
							break;
						default:
							break;
						}

					}catch(Exception e)
					{
						System.out.println(meta + " " + e.getMessage());
						e.printStackTrace();
					}
					catch (Error e) {
						e.printStackTrace();
					}
				}
				bufferedReader.close();
			}
			fileName = rootDir + "/" + parseDate + "/" + outputMetaFile; ;
			NTPLogger.info("Writing file " + fileName);
			fileWriter = new CsvWriter(fileName);
			fileWriter.write("Ticker");
			fileWriter.write("Protocol");
			fileWriter.write("Description");
			fileWriter.write("SourceSymbol");
			fileWriter.write("PrimaryMarket");
			fileWriter.write("RoundLotSize");
			fileWriter.write("IssueType");
			fileWriter.write("FinancialStatus");
			fileWriter.write("Tier");
			fileWriter.write("DisclosureStatus");
			fileWriter.write("AssetClass");
			fileWriter.write("Authenticity");
			fileWriter.write("ShortSaleRestricted");
			fileWriter.write("CaveatEmptor");
			fileWriter.write("Classification");
			fileWriter.write("MarketCategory");
			fileWriter.write("Currency");
			fileWriter.write("Cusip");
			fileWriter.write("PiggybackExempt");
			fileWriter.write("Regsho");
			fileWriter.write("UnsolicitedOnly");
			fileWriter.write("BbQuoted");
			fileWriter.write("MessagingDisabled");
			fileWriter.endRecord();
			for(String key : beanMap.keySet())
			{
				EquityMetaBean bean = beanMap.get(key);
				fileWriter.write(bean.getTicker());
				fileWriter.write(bean.getProtocol());
				fileWriter.write(bean.getDescription());
				fileWriter.write(bean.getSourceSymbol());
				fileWriter.write(bean.getPrimaryMarket());
				fileWriter.write(bean.getRoundLotSize());
				fileWriter.write(bean.getIssueType());
				fileWriter.write(bean.getFinancialStatus());
				fileWriter.write(bean.getTier());
				fileWriter.write(bean.getDisclosureStatus());
				fileWriter.write(bean.getAssetClass());
				fileWriter.write(bean.getAuthenticity());
				fileWriter.write(bean.getShortSaleRestricted());
				fileWriter.write(bean.getCaveatEmptor());
				fileWriter.write(bean.getClassification());
				fileWriter.write(bean.getMarketCategory());
				fileWriter.write(bean.getCurrency());
				fileWriter.write(bean.getCusip());
				fileWriter.write(bean.getPiggybackExempt());
				fileWriter.write(bean.getRegSho());
				fileWriter.write(bean.getUnsolicitedOnly());
				fileWriter.write(bean.getBbQuoted());
				fileWriter.write(bean.getMessagingDisabled());
				fileWriter.endRecord();
			}
			fileWriter.close();
			
			fileName = rootDir + "/" + parseDate + "/" + outputPrimaryMCFile; ;
			NTPLogger.info("Writing file " + fileName);
			fileWriter = new CsvWriter(fileName);
			for(String t : primaryMarketMap.keySet())
			{
				fileWriter.write(t);
				fileWriter.write(primaryMarketMap.get(t));
				fileWriter.endRecord();
			}
			fileWriter.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		catch (Error e) {
			e.printStackTrace();
		}
		NTPLogger.info("Process completed");
		System.exit(-1);
	}
}
