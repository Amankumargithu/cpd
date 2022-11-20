/**
 *  MarketScannerLists.java
 *
 *  Copyright (c) 2003 Tacpoint Technologies, Inc.
 *  All Rights Reserved.
 */
package com.b4utrade.web.helper;

import com.tacpoint.common.DefaultObject;

import java.util.ArrayList;

import org.apache.struts.util.LabelValueBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class assemble lists for the Market Scanner page.
 */
public class MarketScannerLists extends DefaultObject
{

   static Log log = LogFactory.getLog(MarketScannerLists.class);


   /**
    * Creates an ArrayList of Industries.
    *
    * @return ArrayList of industry labels and values.
    */
   public static ArrayList createIndustryList()
   {
      ArrayList industryList = new ArrayList();
      
      //industryList.add(new LabelValueBean("&nbsp;&nbsp;&nbsp;&nbsp;All Industries","INDUSTRY_0"));
      
      /* Old list with DJGCS code
      industryList.add(new LabelValueBean("Industrial Equipment","INDUSTRY_2"));
      industryList.add(new LabelValueBean("Advertising","INDUSTRY_3"));
      industryList.add(new LabelValueBean("Aerospace ","INDUSTRY_4"));
      industryList.add(new LabelValueBean("Airlines","INDUSTRY_5"));
      industryList.add(new LabelValueBean("Auto Parts & Tires","INDUSTRY_6"));
      industryList.add(new LabelValueBean("Auto Parts","INDUSTRY_7"));
      industryList.add(new LabelValueBean("Tires","INDUSTRY_8"));
      industryList.add(new LabelValueBean("Auto Manufacturers","INDUSTRY_9"));
      industryList.add(new LabelValueBean("Banks","INDUSTRY_10"));
      industryList.add(new LabelValueBean("Banks Ex-S&L","INDUSTRY_11"));
      industryList.add(new LabelValueBean("Savings & Loan ","INDUSTRY_12"));
      industryList.add(new LabelValueBean("Beverage","INDUSTRY_13"));
      industryList.add(new LabelValueBean("Distillers & Brewers","INDUSTRY_14"));
      industryList.add(new LabelValueBean("Soft Drinks","INDUSTRY_15"));
      industryList.add(new LabelValueBean("Biotechnology","INDUSTRY_16"));
      industryList.add(new LabelValueBean("Broadcasting ","INDUSTRY_17"));
      industryList.add(new LabelValueBean("Building Materials","INDUSTRY_18"));
      industryList.add(new LabelValueBean("Chemicals","INDUSTRY_19"));
      industryList.add(new LabelValueBean("Chemicals Commodity","INDUSTRY_20"));
      industryList.add(new LabelValueBean("Chemicals Specialty","INDUSTRY_21"));
      industryList.add(new LabelValueBean("Communications Tech.","INDUSTRY_22"));
      industryList.add(new LabelValueBean("Consumer Services","INDUSTRY_23"));
      industryList.add(new LabelValueBean("Containers & Packaging","INDUSTRY_24"));
      industryList.add(new LabelValueBean("Cosmetics","INDUSTRY_25"));
      industryList.add(new LabelValueBean("Diversified Financial","INDUSTRY_26"));
      industryList.add(new LabelValueBean("Electric Components ","INDUSTRY_27"));
      industryList.add(new LabelValueBean("Electric Utilities","INDUSTRY_28"));
      industryList.add(new LabelValueBean("Energy","INDUSTRY_29"));
      industryList.add(new LabelValueBean("Coal","INDUSTRY_30"));
      industryList.add(new LabelValueBean("Oil Companies, Major","INDUSTRY_31"));
      industryList.add(new LabelValueBean("Oil Companies, Secondary","INDUSTRY_32"));
      industryList.add(new LabelValueBean("Oil Drilling Equip/Svs.","INDUSTRY_33"));
      industryList.add(new LabelValueBean("Pipelines","INDUSTRY_34"));
      industryList.add(new LabelValueBean("Entertainment","INDUSTRY_35"));
      industryList.add(new LabelValueBean("Fixed-Line Communications","INDUSTRY_36"));
      industryList.add(new LabelValueBean("Food","INDUSTRY_37"));
      industryList.add(new LabelValueBean("Agriculture","INDUSTRY_38"));
      industryList.add(new LabelValueBean("Food Products","INDUSTRY_39"));
      industryList.add(new LabelValueBean("Food Retailers/Wholesalers","INDUSTRY_40"));
      industryList.add(new LabelValueBean("Forest Products & Paper","INDUSTRY_41"));
      industryList.add(new LabelValueBean("Forest Products","INDUSTRY_42"));
      industryList.add(new LabelValueBean("Paper Products","INDUSTRY_43"));
      industryList.add(new LabelValueBean("Gas Utilities","INDUSTRY_44"));
      industryList.add(new LabelValueBean("General Industrial Svs.","INDUSTRY_45"));
      industryList.add(new LabelValueBean("Industrial Services","INDUSTRY_46"));
      industryList.add(new LabelValueBean("Pollution Control","INDUSTRY_47"));
      industryList.add(new LabelValueBean("Healthcare Providers","INDUSTRY_48"));
      industryList.add(new LabelValueBean("Heavy Construction","INDUSTRY_49"));
      industryList.add(new LabelValueBean("Home Construction/Furnishing","INDUSTRY_50"));
      industryList.add(new LabelValueBean("Furnishings & Appliances","INDUSTRY_51"));
      industryList.add(new LabelValueBean("Home Construction","INDUSTRY_52"));
      industryList.add(new LabelValueBean("Household Products","INDUSTRY_53"));
      industryList.add(new LabelValueBean("Household Prod. Durable","INDUSTRY_54"));
      industryList.add(new LabelValueBean("Household Prod. Nondurable","INDUSTRY_55"));
      industryList.add(new LabelValueBean("Industrial Diversified","INDUSTRY_56"));
      industryList.add(new LabelValueBean("Industrial Equipment","INDUSTRY_57"));
      industryList.add(new LabelValueBean("Factory Equipment","INDUSTRY_58"));
      industryList.add(new LabelValueBean("Heavy Machinery","INDUSTRY_59"));
      industryList.add(new LabelValueBean("Industrial Transportation","INDUSTRY_60"));
      industryList.add(new LabelValueBean("Air Freight","INDUSTRY_61"));
      industryList.add(new LabelValueBean("Land Transportation Equip.","INDUSTRY_62"));
      industryList.add(new LabelValueBean("Marine Transport","INDUSTRY_63"));
      industryList.add(new LabelValueBean("Railroads","INDUSTRY_64"));
      industryList.add(new LabelValueBean("Shipbuilding","INDUSTRY_65"));
      industryList.add(new LabelValueBean("Transportation Services","INDUSTRY_66"));
      industryList.add(new LabelValueBean("Trucking","INDUSTRY_67"));
      industryList.add(new LabelValueBean("Insurance","INDUSTRY_68"));
      industryList.add(new LabelValueBean("Insurance Full-Line","INDUSTRY_69"));
      industryList.add(new LabelValueBean("Insurance Life","INDUSTRY_70"));
      industryList.add(new LabelValueBean("Insurance Prop. & Casualty","INDUSTRY_71"));
      industryList.add(new LabelValueBean("Internet & Online","INDUSTRY_72"));
      industryList.add(new LabelValueBean("Internet Commerce","INDUSTRY_73"));
      industryList.add(new LabelValueBean("Internet Services","INDUSTRY_74"));
      industryList.add(new LabelValueBean("Online Brokerages","INDUSTRY_75"));
      industryList.add(new LabelValueBean("Leisure Goods & Services","INDUSTRY_76"));
      industryList.add(new LabelValueBean("Casinos","INDUSTRY_77"));
      industryList.add(new LabelValueBean("Consumer Electronics","INDUSTRY_78"));
      industryList.add(new LabelValueBean("Lodging","INDUSTRY_79"));
      industryList.add(new LabelValueBean("Recreational Prod. & Svcs.","INDUSTRY_80"));
      industryList.add(new LabelValueBean("Restaurants","INDUSTRY_81"));
      industryList.add(new LabelValueBean("Toys","INDUSTRY_82"));
      industryList.add(new LabelValueBean("Medical Products","INDUSTRY_83"));
      industryList.add(new LabelValueBean("Advanced Medical Devices","INDUSTRY_84"));
      industryList.add(new LabelValueBean("Medical Supplies","INDUSTRY_85"));
      industryList.add(new LabelValueBean("Mining & Metals","INDUSTRY_86"));
      industryList.add(new LabelValueBean("Aluminum","INDUSTRY_87"));
      industryList.add(new LabelValueBean("Mining","INDUSTRY_88"));
      industryList.add(new LabelValueBean("Nonferrous Metals","INDUSTRY_89"));
      industryList.add(new LabelValueBean("Precious Metals","INDUSTRY_90"));
      industryList.add(new LabelValueBean("Steel","INDUSTRY_91"));
      industryList.add(new LabelValueBean("Pharmaceuticals","INDUSTRY_92"));
      industryList.add(new LabelValueBean("Publishing","INDUSTRY_93"));
      industryList.add(new LabelValueBean("Real Estate","INDUSTRY_94"));
      industryList.add(new LabelValueBean("Retail","INDUSTRY_95"));
      industryList.add(new LabelValueBean("Retailers Appareal","INDUSTRY_96"));
      industryList.add(new LabelValueBean("Retailers Broadline","INDUSTRY_97"));
      industryList.add(new LabelValueBean("Retailers Drug-Based","INDUSTRY_98"));
      industryList.add(new LabelValueBean("Retailers Speciality","INDUSTRY_99"));
      industryList.add(new LabelValueBean("Securities Brokers","INDUSTRY_100"));
      industryList.add(new LabelValueBean("Semiconductors","INDUSTRY_101"));
      industryList.add(new LabelValueBean("Software","INDUSTRY_102"));
      industryList.add(new LabelValueBean("Tech. Hardware & Equip.","INDUSTRY_103"));
      industryList.add(new LabelValueBean("Computers","INDUSTRY_104"));
      industryList.add(new LabelValueBean("Office Equipment","INDUSTRY_105"));
      industryList.add(new LabelValueBean("Technology Services","INDUSTRY_106"));
      industryList.add(new LabelValueBean("Diversified Tech. Svcs.","INDUSTRY_107"));
      industryList.add(new LabelValueBean("Internet Svcs.","INDUSTRY_108"));
      industryList.add(new LabelValueBean("Textiles & Apparel","INDUSTRY_109"));
      industryList.add(new LabelValueBean("Clothing & Fabrics","INDUSTRY_110"));
      industryList.add(new LabelValueBean("Footwear","INDUSTRY_111"));
      industryList.add(new LabelValueBean("Tobacco","INDUSTRY_112"));
      industryList.add(new LabelValueBean("Water Utilities","INDUSTRY_113"));
      industryList.add(new LabelValueBean("Wireless Communications","INDUSTRY_114"));
      */
      
      // New list with ICB codes
      /*
      industryList.add(new LabelValueBean("Aerospace & Defense", "INDUSTRY_3867"));
      industryList.add(new LabelValueBean("Automobiles & Parts", "INDUSTRY_3891"));
      industryList.add(new LabelValueBean("Banks", "INDUSTRY_3954"));
      industryList.add(new LabelValueBean("Beverages", "INDUSTRY_3895"));
      industryList.add(new LabelValueBean("Chemicals", "INDUSTRY_3848"));
      industryList.add(new LabelValueBean("Construction & Materials", "INDUSTRY_3864"));
      industryList.add(new LabelValueBean("Electricity", "INDUSTRY_3948"));
      industryList.add(new LabelValueBean("Electronic & Electrical Equipment", "INDUSTRY_3873"));
      industryList.add(new LabelValueBean("Equity Investment Instruments", "INDUSTRY_3972"));
      industryList.add(new LabelValueBean("Fixed Line Telecommunications", "INDUSTRY_3944"));
      industryList.add(new LabelValueBean("Food & Drug Retailers", "INDUSTRY_3924"));
      industryList.add(new LabelValueBean("Food Producers", "INDUSTRY_3899"));
      industryList.add(new LabelValueBean("Forestry & Paper", "INDUSTRY_3851"));
      industryList.add(new LabelValueBean("Gas, Water & Multiutilities", "INDUSTRY_3950"));
      industryList.add(new LabelValueBean("General Financial", "INDUSTRY_3966"));
      industryList.add(new LabelValueBean("General Industrials", "INDUSTRY_3870"));
      industryList.add(new LabelValueBean("General Retailers", "INDUSTRY_3927"));
      industryList.add(new LabelValueBean("Health Care Equipment & Services", "INDUSTRY_3917"));
      industryList.add(new LabelValueBean("Household Goods", "INDUSTRY_3902"));
      industryList.add(new LabelValueBean("Industrial Engineering", "INDUSTRY_3876"));
      industryList.add(new LabelValueBean("Industrial Metals", "INDUSTRY_3854"));
      industryList.add(new LabelValueBean("Industrial Transportation", "INDUSTRY_3879"));
      industryList.add(new LabelValueBean("Leisure Goods", "INDUSTRY_3907"));
      industryList.add(new LabelValueBean("Life Insurance", "INDUSTRY_3961"));
      industryList.add(new LabelValueBean("Media", "INDUSTRY_3933"));
      industryList.add(new LabelValueBean("Mining", "INDUSTRY_3858"));
      industryList.add(new LabelValueBean("Mobile Telecommunications", "INDUSTRY_3946"));
      industryList.add(new LabelValueBean("Nonequity Investment Instruments", "INDUSTRY_3974"));
      industryList.add(new LabelValueBean("Nonlife Insurance", "INDUSTRY_3956"));
      industryList.add(new LabelValueBean("Oil & Gas Producers", "INDUSTRY_3842"));
      industryList.add(new LabelValueBean("Oil Equipment & Services", "INDUSTRY_3845"));
      industryList.add(new LabelValueBean("Personal Goods", "INDUSTRY_3911"));
      industryList.add(new LabelValueBean("Pharmaceuticals & Biotechnology", "INDUSTRY_3921"));
      industryList.add(new LabelValueBean("Real Estate", "INDUSTRY_3963"));
      industryList.add(new LabelValueBean("Software & Computer Services", "INDUSTRY_3976"));
      industryList.add(new LabelValueBean("Support Services", "INDUSTRY_3885"));
      industryList.add(new LabelValueBean("Technology Hardware & Equipment", "INDUSTRY_3980"));
      industryList.add(new LabelValueBean("Tobacco", "INDUSTRY_3915"));
      industryList.add(new LabelValueBean("Travel & Leisure", "INDUSTRY_3937"));
      */

/*
 * industry list for market scanner to get sector
 *  */
      industryList.add(new LabelValueBean("Insurance","Insurance"));
      industryList.add(new LabelValueBean("Retail","Retail"));
      industryList.add(new LabelValueBean("Household & Personal Products","Household & Personal Products"));
      industryList.add(new LabelValueBean("Paper & Forest Products","Paper & Forest Products"));
      industryList.add(new LabelValueBean("Banking & Savings","Banking & Savings"));
      industryList.add(new LabelValueBean("Transport","Transport"));
      industryList.add(new LabelValueBean("Communications","Communications"));
      industryList.add(new LabelValueBean("Appliances & Electronics","Appliances & Electronics"));
      industryList.add(new LabelValueBean("IT Services & Software","IT Services & Software"));
      industryList.add(new LabelValueBean("Chemicals","Chemicals"));
      industryList.add(new LabelValueBean("IT Hardware","IT Hardware"));
      industryList.add(new LabelValueBean("Consumer Services","Consumer Services"));
      industryList.add(new LabelValueBean("Agriculture","Agriculture"));
      industryList.add(new LabelValueBean("Aerospace & Defense","Aerospace & Defense"));
      industryList.add(new LabelValueBean("Tobacco","Tobacco"));
      industryList.add(new LabelValueBean("Food & Beverage","Food & Beverage"));
      industryList.add(new LabelValueBean("Water Utilities","Water Utilities"));
      industryList.add(new LabelValueBean("Metals & Mining","Metals & Mining"));
      industryList.add(new LabelValueBean("Real Estate","Real Estate"));
      industryList.add(new LabelValueBean("Miscellaneous","Miscellaneous"));
      industryList.add(new LabelValueBean("Electric Utilities","Electric Utilities"));
      industryList.add(new LabelValueBean("Gas Utilities","Gas Utilities"));
      industryList.add(new LabelValueBean("Consumer Goods","Consumer Goods"));
      industryList.add(new LabelValueBean("Construction Materials","Construction Materials"));
      industryList.add(new LabelValueBean("Health Care Services","Health Care Services"));
      industryList.add(new LabelValueBean("Synthetic Materials","Synthetic Materials"));
      industryList.add(new LabelValueBean("Media","Media"));
      industryList.add(new LabelValueBean("Investments & Brokers","Investments & Brokers"));
      industryList.add(new LabelValueBean("Pharmaceuticals & Biotechnology","Pharmaceuticals & Biotechnology"));
      industryList.add(new LabelValueBean("Textiles & Apparel","Textiles & Apparel"));
      industryList.add(new LabelValueBean("Oil & Gas","Oil & Gas"));
      industryList.add(new LabelValueBean("Health Care Equipment","Health Care Equipment"));
      industryList.add(new LabelValueBean("Machinery","Machinery"));
      industryList.add(new LabelValueBean("Electrical Equipment","Electrical Equipment"));
      industryList.add(new LabelValueBean("Autos & Auto Parts","Autos & Auto Parts"));
      industryList.add(new LabelValueBean("Commercial Services & Supplies","Commercial Services & Supplies"));
      industryList.add(new LabelValueBean("Credit & Lending","Credit & Lending"));
      

      return industryList;
   }

   /**
    * Creates an ArrayList of Exchanges.
    *
    * @return ArrayList of exchange labels and values.
    */
   public static ArrayList createExchangeList()
   {
      ArrayList exchangeList = new ArrayList();

      exchangeList.add(new LabelValueBean("All Xchgs", "EXCHANGE_0"));
      exchangeList.add(new LabelValueBean("NYSE", "EXCHANGE_2"));
      exchangeList.add(new LabelValueBean("NASDAQ", "EXCHANGE_3"));
      exchangeList.add(new LabelValueBean("AMEX", "EXCHANGE_4"));
      exchangeList.add(new LabelValueBean("OTC", "EXCHANGE_5"));

      return exchangeList;
   }

   /**
    * Creates an ArrayList of Prices.
    *
    * @return ArrayList of Price labels and values.
    */
   public static ArrayList createPriceList()
   {
      ArrayList priceList = new ArrayList();

      priceList.add(new LabelValueBean("All Prices", "PRICE_0"));
      priceList.add(new LabelValueBean("0-5", "PRICE_2"));
      priceList.add(new LabelValueBean("5-15", "PRICE_3"));
      priceList.add(new LabelValueBean("15-25", "PRICE_4"));
      priceList.add(new LabelValueBean("25-50", "PRICE_5"));
      priceList.add(new LabelValueBean("50-100", "PRICE_6"));
      priceList.add(new LabelValueBean(">100", "PRICE_7"));


      return priceList;
   }

   /**
    * Creates an ArrayList of Market Caps.
    *
    * @return ArrayList of Market Cap labels and values.
    */
   public static ArrayList createMarketCapList()
   {
      ArrayList marketCapList = new ArrayList();

      marketCapList.add(new LabelValueBean("All Mkt Caps", "MKTCAP_0"));
      marketCapList.add(new LabelValueBean("SMALL", "MKTCAP_2"));
      marketCapList.add(new LabelValueBean("MID", "MKTCAP_3"));
      marketCapList.add(new LabelValueBean("LARGE", "MKTCAP_4"));

      return marketCapList;
   }

   /**
    * Creates an ArrayList of various Top Ten items.
    *
    * @return ArrayList of Top Ten labels and values.
    */
   public static ArrayList createTopTenList()
   {
      ArrayList topTenList = new ArrayList();

      topTenList.add(new LabelValueBean("Most Active", "TOPTEN_1"));
//      topTenList.add(new LabelValueBean("Active w/ News", "TOPTEN_2"));
      topTenList.add(new LabelValueBean("$ Gainers", "TOPTEN_3"));
      topTenList.add(new LabelValueBean("$ Losers", "TOPTEN_4"));
      topTenList.add(new LabelValueBean("% Gainers", "TOPTEN_5"));
      topTenList.add(new LabelValueBean("% Losers", "TOPTEN_6"));
      topTenList.add(new LabelValueBean("New 52 wk Hi's", "TOPTEN_8"));
      topTenList.add(new LabelValueBean("New 52 wk Lows", "TOPTEN_9"));

      return topTenList;
   }



}