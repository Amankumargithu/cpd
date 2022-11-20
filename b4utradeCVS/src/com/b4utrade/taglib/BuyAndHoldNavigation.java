package com.b4utrade.taglib;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import com.tacpoint.util.*;

/**
 *
 */

public class BuyAndHoldNavigation extends TagSupport {

    private String selectedTab = new String();
    private boolean disableLink = false;
    private boolean userLoggedIn = true;
    private String targetTag = new String(" ");

    /**
      * Getter/Setter for the attribute tab as defined in the tld file 
      * for this tag
      */
    public void setTab(String value){
        
        if (value != null) {
            
            selectedTab = value;
    
        }
    
    }

    public String getName(){
    
        return selectedTab;
    
    }

    /**
      * Getter/Setter for the attribute disableLink as defined in the tld file 
      * for this tag
      */
    public void setDisableLink(String value) {
        
        if ((value == null) || (value.equals(""))) {
            
            disableLink = false;
            
        }
        else if (value.equalsIgnoreCase("true")) {

            disableLink = true;
            
        }
        else {
            
            disableLink = false;
            
        }
            
    }

    public String getDisableLink() {
    
        return String.valueOf(disableLink);
    
    }

    /**
      * Getter/Setter for the attribute loggedIn as defined in the tld file 
      * for this tag
      */
    public void setLoggedIn(String value) {
        
        if ((value == null) || (value.equals(""))) {
            
            userLoggedIn = true;
            
        }
        else if (value.equalsIgnoreCase("false")) {

            userLoggedIn = false;
            
        }
        else {
            
            userLoggedIn = true;
            
        }
            
    }

    public String getLoggedIn() {
    
        return String.valueOf(userLoggedIn);
    
    }

    public void setFrameTarget(String value){
        
        if (value != null) {
            
            targetTag = "target=\"" + value + "\"";
    
        }
    
    }

    public String getFrameTarget(){
    
        return targetTag;
    
    }

/**
* doStartTag is called by the JSP container when the tag is encountered
*/
    public int doStartTag() {
        
        try {

            String wallOfStocksURL = Environment.get("BUYANDHOLD_WALL_OF_STOCKS_URL");
            String todaysTop10URL = Environment.get("BUYANDHOLD_TODAYS_TOP_10_URL");
            String streamingPortfoliosURL = Environment.get("BUYANDHOLD_STREAMING_PORTFOLIOS_URL");
            String stocksUpCloseURL = Environment.get("BUYANDHOLD_STOCKS_UP_CLOSE_URL");
            String secureURL = Environment.get("B4UTRADE_SECURE_URL");
            
            JspWriter out = pageContext.getOut();

            out.println("<TABLE WIDTH=\"100%\" BORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"0\" BGCOLOR=\"#003300\">");
            out.println("    <TR>");

            if ((selectedTab.equalsIgnoreCase("wallofstocks")) && (disableLink)) {
                out.println("        <TD align=\"center\" valign=\"middle\" height=\"34\"><font id=\"link2\"><b>Wall of Stocks</b></font></TD>");
            }
            else if (selectedTab.equalsIgnoreCase("wallofstocks")) {
                out.println("        <TD align=\"center\" valign=\"middle\" height=\"34\"><a href=\"" + wallOfStocksURL + "\"" + targetTag + "><font id=\"link2\"><b>Wall of Stocks</b></font></a></TD>");
            }
            else {
                out.println("        <TD align=\"center\" valign=\"middle\" height=\"34\"><a href=\"" + wallOfStocksURL + "\"" + targetTag + "><font id=\"link\"><b>Wall of Stocks</b></font></a></TD>");
            }

            if ((selectedTab.equalsIgnoreCase("todaystop10")) && (disableLink)) {
                out.println("        <TD align=\"center\" valign=\"middle\" height=\"34\"><font id=\"link2\"><b>Market Scanner</b></font></TD>");
            }
            else if (selectedTab.equalsIgnoreCase("todaystop10")) {
                out.println("        <TD align=\"center\" valign=\"middle\" height=\"34\"><a href=\"" + todaysTop10URL + "\"" + targetTag + "><font id=\"link2\"><b>Market Scanner</b></font></a></TD>");
            }
            else {
                out.println("        <TD align=\"center\" valign=\"middle\" height=\"34\"><a href=\"" + todaysTop10URL + "\"" + targetTag + "><font id=\"link\"><b>Market Scanner</b></font></a></TD>");
            }

            if ((selectedTab.equalsIgnoreCase("streamingportfolios")) && (disableLink)) {
                out.println("        <TD align=\"center\" valign=\"middle\" height=\"34\"><font id=\"link2\"><b>Portfolio Tracker</b></font></TD>");
            }
            else if (selectedTab.equalsIgnoreCase("streamingportfolios")) {
                out.println("        <TD align=\"center\" valign=\"middle\" height=\"34\"><a href=\"" + streamingPortfoliosURL + "\"" + targetTag + "><font id=\"link2\"><b>Portfolio Tracker</b></font></a></TD>");
            }
            else {
                out.println("        <TD align=\"center\" valign=\"middle\" height=\"34\"><a href=\"" + streamingPortfoliosURL + "\"" + targetTag + "><font id=\"link\"><b>Portfolio Tracker</b></font></a></TD>");
            }

            if ((selectedTab.equalsIgnoreCase("stocksupclose")) && (disableLink)) {
                out.println("        <TD align=\"center\" valign=\"middle\" height=\"34\"><font id=\"link2\"><b>Stocks Up Close</b></font></TD>");
            }
            else if (selectedTab.equalsIgnoreCase("stocksupclose")) {
                out.println("        <TD align=\"center\" valign=\"middle\" height=\"34\"><a href=\"" + stocksUpCloseURL + "\"" + targetTag + "><font id=\"link2\"><b>Stocks Up Close</b></font></a></TD>");
            }
            else {
                out.println("        <TD align=\"center\" valign=\"middle\" height=\"34\"><a href=\"" + stocksUpCloseURL + "\"" + targetTag + "><font id=\"link\"><b>Stocks Up Close</b></font></a></TD>");
            }

        } 
        catch (Exception e) {
            throw new Error("Exception Caught: " + e.getMessage());
        }
      
      // Must return SKIP_BODY because we are not supporting a body for this 
      // tag.
      
      return SKIP_BODY;
    }

/**
 * doEndTag is called by the JSP container when the tag is closed
 */
    public int doEndTag(){
       
        try {

            JspWriter out = pageContext.getOut();

            out.println("    </TR>");
            out.println("</table>");
        
        } 
        catch (Exception e) {
            throw new Error("Exception Caught: " + e.getMessage());
        }

      return SKIP_BODY;
    
    }
}



