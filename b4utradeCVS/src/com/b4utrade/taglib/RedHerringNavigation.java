package com.b4utrade.taglib;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import com.tacpoint.util.*;

/**
 *
 */

public class RedHerringNavigation extends TagSupport {

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

            String wallOfStocksURL = Environment.get("REDHERRING_WALL_OF_STOCKS_URL");
            String todaysTop10URL = Environment.get("REDHERRING_TODAYS_TOP_10_URL");
            String streamingPortfoliosURL = Environment.get("REDHERRING_STREAMING_PORTFOLIOS_URL");
            String stocksUpCloseURL = Environment.get("REDHERRING_STOCKS_UP_CLOSE_URL");
            String secureURL = Environment.get("B4UTRADE_SECURE_URL");
            
            JspWriter out = pageContext.getOut();

            out.println("<SCRIPT LANGUAGE = \"javascript\"><!--");
            out.println("if (document.images) {          ");
            out.println("img1on = new Image(); img1on.src = \"/redherring/images/nav_wall_on.GIF\";");
            out.println("img1off = new Image(); img1off.src = \"/redherring/images/nav_wall.GIF\";");    
            out.println("img2on = new Image(); img2on.src=\"/redherring/images/nav_scanner_on.GIF\";");
            out.println("img2off = new Image(); img2off.src=\"/redherring/images/nav_scanner.GIF\";");
            out.println("img3on = new Image(); img3on.src = \"/redherring/images/nav_tracker_on.GIF\";");
            out.println("img3off = new Image(); img3off.src = \"/redherring/images/nav_tracker.GIF\";");
            out.println("img4on = new Image(); img4on.src = \"/redherring/images/nav_stocks_on.GIF\";");
            out.println("img4off = new Image(); img4off.src = \"/redherring/images/nav_stocks.GIF\"; }");
            out.println("// Function to 'activate' images.");
            out.println("function imgOn(imgName) {");
            out.println("        if (document.images) {");
            out.println("            document[imgName].src = eval(imgName + \"on.src\");");
            out.println("        }");
            out.println("}");
            out.println("// Function to 'deactivate' images.");
            out.println("function imgOff(imgName) {");
            out.println("        if (document.images) {");
            out.println("            document[imgName].src = eval(imgName + \"off.src\");");
            out.println("        }");
            out.println("}// -->");
            out.println("</SCRIPT>");

            out.println("<TABLE WIDTH=\"100%\" BORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"0\">");
            out.println("    <tr><td height=5 colspan=4><img src=\"/redherring/images/spacer.GIF\" width=\"12\" height=\"5\"></td></tr>");
            out.println("    <TR>");

            if ((selectedTab.equalsIgnoreCase("wallofstocks")) && (disableLink)) {
                out.println("        <td><img src=\"/redherring/images/nav_wall_on.GIF\" width=117 height=18 border=0 alt=\"\" name=img1></td>");
            }
            else if (selectedTab.equalsIgnoreCase("wallofstocks")) {
                out.println("        <td><a href=\"" + wallOfStocksURL + "\"" + targetTag + "><img src=\"/redherring/images/nav_wall_on.GIF\" width=117 height=18 border=0 alt=\"\" name=img1></a></td>");
            }
            else {
                out.println("        <td><a href=\"" + wallOfStocksURL + "\"" + targetTag + " onMouseOver=\"imgOn('img1')\" onMouseOut=\"imgOff('img1')\"><img src=\"/redherring/images/nav_wall.GIF\" width=117 height=18 border=0 alt=\"\" name=img1></a></td>");
            }

            if ((selectedTab.equalsIgnoreCase("todaystop10")) && (disableLink)) {
                out.println("        <td><img src=\"/redherring/images/nav_scanner_on.GIF\" width=126 height=18 border=0 alt=\"\" name=img2></td>");
            }
            else if (selectedTab.equalsIgnoreCase("todaystop10")) {
                out.println("        <td><a href=\"" + todaysTop10URL + "\"" + targetTag + "><img src=\"/redherring/images/nav_scanner_on.GIF\" width=126 height=18 border=0 alt=\"\" name=img2></a></td>");
            }
            else {
                out.println("        <td><a href=\"" + todaysTop10URL + "\"" + targetTag + " onMouseOver=\"imgOn('img2')\" onMouseOut=\"imgOff('img2')\"><img src=\"/redherring/images/nav_scanner.GIF\" width=126 height=18 border=0 alt=\"\" name=img2></a></td>");
            }

            if ((selectedTab.equalsIgnoreCase("streamingportfolios")) && (disableLink)) {
                out.println("        <td><img src=\"/redherring/images/nav_tracker_on.GIF\" width=137 height=18 border=0 alt=\"\" name=img3></td>");
            }
            else if (selectedTab.equalsIgnoreCase("streamingportfolios")) {
                out.println("        <td><a href=\"" + streamingPortfoliosURL + "\"" + targetTag + "><img src=\"/redherring/images/nav_tracker_on.GIF\" width=137 height=18 border=0 alt=\"\" name=img3></a></td>");
            }
            else {
                out.println("        <td><a href=\"" + streamingPortfoliosURL + "\"" + targetTag + " onMouseOver=\"imgOn('img3')\" onMouseOut=\"imgOff('img3')\"><img src=\"/redherring/images/nav_tracker.GIF\" width=137 height=18 border=0 alt=\"\" name=img3></a></td>");
            }

            if ((selectedTab.equalsIgnoreCase("stocksupclose")) && (disableLink)) {
                out.println("        <td><img src=\"/redherring/images/nav_stocks_on.GIF\" width=119 height=18 border=0 alt=\"\" name=img4></td>");
            }
            else if (selectedTab.equalsIgnoreCase("stocksupclose")) {
                out.println("        <td><a href=\"" + stocksUpCloseURL + "\"" + targetTag + "><img src=\"/redherring/images/nav_stocks_on.GIF\" width=119 height=18 border=0 alt=\"\" name=img4></a></td>");
            }
            else {
                out.println("        <td><a href=\"" + stocksUpCloseURL + "\"" + targetTag + " onMouseOver=\"imgOn('img4')\" onMouseOut=\"imgOff('img4')\"><img src=\"/redherring/images/nav_stocks.GIF\" width=119 height=18 border=0 alt=\"\" name=img4></a></td>");
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
            out.println("    <tr><td colspan=4 bgcolor=cc9933 height=5><spacer type=block height=5 width=5></td></tr>");
            out.println("</table>");
        
        } 
        catch (Exception e) {
            throw new Error("Exception Caught: " + e.getMessage());
        }

      return SKIP_BODY;
    
    }
}



