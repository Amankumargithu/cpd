package ntp.uf3;

import java.util.LinkedHashSet;

import ntp.logger.NTPLogger;

public class RecorderMetaHelper {

	public static LinkedHashSet<Byte> updateMetaHeaders(String fileType, LinkedHashSet<Byte> metaType)
	{
		LinkedHashSet<Byte> metaorder = new LinkedHashSet<>();
		switch (fileType) {
		case "UTP":
			metaorder.add((byte) 0xfb);	//Product Type
			metaorder.add((byte) 0xfd);	//Currency
			metaorder.add((byte) 0xfe);	//Country
			metaorder.add((byte) 0x0d);	//Market Tier
			metaorder.add((byte) 0x19);	//Listing Market
			metaorder.add((byte) 0xf8);	//Primary Market Code
			metaorder.add((byte) 0x0f);	//Authenticity
			metaorder.add((byte) 0x1a);	//Round Lot
			metaorder.add((byte) 0x2e);	//Issue Name
			metaorder.add((byte) 0x2f);	//old Symbol
			metaorder.add((byte) 0x30);	//Issue Type			
			break;
		case "CTA-A":
		case "CTA-B":
			metaorder.add((byte) 0xfb);	//Product Type
			metaorder.add((byte) 0xfd);	//Currency
			metaorder.add((byte) 0xfe);	//Country
			metaorder.add((byte) 0x0d);	//Market Tier
			metaorder.add((byte) 0x19);	//Listing Market
			metaorder.add((byte) 0xf8);	//Primary Market Code
			metaorder.add((byte) 0xf9);	//SymbologyType
			metaorder.add((byte) 0xfa);	//AlternateSymbol
			metaorder.add((byte) 0x0e);	//FinancialStatus			
			break;
		case "OTCBB":
			metaorder.add((byte) 0xfb);	//Product Type
			metaorder.add((byte) 0xfd);	//Currency
			metaorder.add((byte) 0xfe);	//Country
			metaorder.add((byte) 0x0d);	//Market Tier
			metaorder.add((byte) 0x2a);	//CaveatEmptor
			metaorder.add((byte) 0x2c);	//BB_Quoted
			metaorder.add((byte) 0x2e);	//Issue Name	
			break;
		case "OTC":
			metaorder.add((byte) 0xfb);	//Product Type
			metaorder.add((byte) 0xfd);	//Currency
			metaorder.add((byte) 0xfe);	//Country
			metaorder.add((byte) 0x0d);	//Market Tier
			metaorder.add((byte) 0x19);	//Listing Market
			metaorder.add((byte) 0xf8);	//Primary Market Code
			metaorder.add((byte) 0x2a);	//CaveatEmptor
			metaorder.add((byte) 0x2c);	//BB_Quoted
			metaorder.add((byte) 0x2e);	//Issue Name
			metaorder.add((byte) 0x10);	//ShortSaleThresholdIndicator
			metaorder.add((byte) 0x29);	//PiggybackExempt
			metaorder.add((byte) 0x2b);	//UnsolicitedOnly
			metaorder.add((byte) 0x2d);	//MessagingDisabled
			metaorder.add((byte) 0xd1);	//SaturationEligibleFlag
			metaorder.add((byte) 0xd0);	//Rule144aFlag
			break;
		default:
			metaorder.addAll(metaType);
			break;
		}
		if(!metaorder.containsAll(metaType))
		{
			NTPLogger.warning(fileType + " meta file have new fields ");
			for(Byte b: metaType)
			{
				if(!metaorder.contains(b))
				{
					NTPLogger.warning("NEW META " + b + " " + RecorderHelper.metaCodeMap.get(b));
					metaorder.add(b);
				}
			}
		}		
		return metaorder;
	}
}
