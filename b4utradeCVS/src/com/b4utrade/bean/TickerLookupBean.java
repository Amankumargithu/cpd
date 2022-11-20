/**
 * TickerLookupBean.java
 *
 * @author Copyright (c) 2003 by Tacpoint Technologies, Inc. All Rights Reserved.
 * @version 1.0
 */

package com.b4utrade.bean;

import com.tacpoint.common.DefaultObject;
import java.util.Vector;


/**
 * This bean holds the ticker lookup Data.
 */
public class TickerLookupBean extends DefaultObject
{
   private static final char STOCK = 'S';
   private static final char MUTUAL_FUND = 'M';
   private static final char INDEX = 'I';
   private static final char ALL = 'A';
   
   private static final boolean UNSUCCESSFUL = false;
   private static final boolean SUCCESSFUL = true;   
	
   private static final int MINI_SEARCH = 5;
   private static final int FULL_SEARCH = 1;
   

   private Vector searchResult = new Vector();    // Vector of DetailedQuoteBean
   private char searchType;
   private String searchCriteria;
   private boolean loadingStatusFlag;   
   private int searchCount;
   
   /**
    *  Default constructor
    */
   public TickerLookupBean()
   {
      searchType = ALL;
      searchCriteria = "";
      loadingStatusFlag = UNSUCCESSFUL;
   }

   /**
    * Sets the searchResult value.
    *
    * @param searchResult Vector
    */
   public void setSearchResult(Vector searchResult) {
      this.searchResult = searchResult;
   }

   /**
    * Gets the searchResult value.
    *
    * @return searchResult  Vector
    */
   public Vector getSearchResult() {
      return searchResult;
   }


   /**
    * set the search criteria.
    */

   public void setSearchCriteria(String searchCriteria)
   {
       this.searchCriteria = searchCriteria;
   }

   public String getSearchCriteria()
   {
       return(this.searchCriteria);
   }

   /**
    * set the search type.
    */

   public void setSearchType(char searchType)
   {
       this.searchType = searchType;
   }

   public char getSearchType()
   {
       return(this.searchType);
   }

   public void setStockSearchType()          { setSearchType(STOCK); }
   public void setMutualFundSearchType()     { setSearchType(MUTUAL_FUND); }
   public void setIndexSearchType()          { setSearchType(INDEX); }
   public void setAllSearchType()            { setSearchType(ALL); }
   
   public boolean isStockSearchType()        { return(STOCK == getSearchType()); }
   public boolean isMutualFundSearchType()   { return(MUTUAL_FUND == getSearchType()); }
   public boolean isIndexSearchType()        { return(INDEX == getSearchType()); }
   public boolean isAllSearchType()          { return(ALL == getSearchType()); }

   /**
    * set the loading Status flag to indicate if the data loading is successfully.
    *
    */

   public void setLoadingStatusFlag(boolean loadingStatusFlag)
   {
       this.loadingStatusFlag = loadingStatusFlag;
   }

   public boolean getLoadingStatusFlag()
   {
       return(this.loadingStatusFlag);
   }

   public void setLoadingSuccessfulFlag()
   {
       setLoadingStatusFlag(SUCCESSFUL);
   }

   public void setLoadingFailedFlag()
   {
       setLoadingStatusFlag(UNSUCCESSFUL);
   }

   public boolean isLoadingSuccessful()
   {
       return(getLoadingStatusFlag());
   }

   public int getSearchCount(){
	   return searchCount;
   }
   
   public void setSearchCount(int count){
	   searchCount = count;
   }

}
