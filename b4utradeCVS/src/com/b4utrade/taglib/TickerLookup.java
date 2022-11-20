package com.b4utrade.taglib;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import java.io.*;
     
import com.tacpoint.util.*;

/**
 *
 */

public class TickerLookup extends BodyTagSupport {

    private String targetWindow = new String();
    private String targetPage = new String();
    private String returnParam = new String();
    private String lookupString = new String();

    /**
      * Getter/Setter for the attribute targetWindow as defined in the tld file 
      * for this tag
      */
    public void setTargetWindow(String value){
        
        if (value != null) {
            
            targetWindow = value;
    
        }
    
    }

    public String getTargetWindow(){
    
        return targetWindow;
    
    }

    /**
      * Getter/Setter for the attribute targetPage as defined in the tld file 
      * for this tag
      */
    public void setTargetPage(String value){
        
        if (value != null) {
            
            targetPage = value;
    
        }
    
    }

    public String getTargetPage(){
    
        return targetPage;
    
    }

    /**
      * Getter/Setter for the attribute returnParam as defined in the tld file 
      * for this tag
      */
    public void setReturnParam(String value){
        
        if (value != null) {
            
            returnParam = value;
    
        }
    
    }

    public String getReturnParam(){
    
        return returnParam;
    
    }


    /**
      * Getter/Setter for the attribute lookupString as defined in the tld file 
      * for this tag
      */
    public void setLookupString(String value){
        
        if (value != null) {
            
            lookupString = value;
    
        }
    
    }

    public String getLookupString(){
    
        return lookupString;
    
    }

/**
* doStartTag is called by the JSP container when the tag is encountered
*/
    public int doStartTag() {
        
        
        // this method was added after the forbes site was built so this check is done
        // to ensure the old code does not break
        if ((lookupString == null) || lookupString.equals("")) {
            lookupString = "FORBES_TICKER_LOOKUP_URL";  
        }

        try {

            String tickerLookupPage = Environment.get(lookupString);
            
            JspWriter out = pageContext.getOut();

            out.print("<A href='" + tickerLookupPage + 
                      "?targetwindow=" + targetWindow + 
                      "&targetservlet=" + targetPage + 
                      "&returnparam=" + returnParam + 
                      "' onClick=\"window.open('','tickerlookup','width=500,height=400','scrollbars=yes')\" TARGET=\"tickerlookup\">");

        } 
        catch (Exception e) {
            throw new Error("Exception Caught: " + e.getMessage());
        }
      
      // Must return SKIP_BODY because we are not supporting a body for this 
      // tag.
      
      return EVAL_BODY_TAG;
    }

/**
 * doEndTag is called by the JSP container when the tag is closed
 */
    public int doEndTag(){
       
        try {
                
            JspWriter out = pageContext.getOut();

            out.println("</a>");
        
        } 
        catch (Exception e) {
            throw new Error("Exception Caught: " + e.getMessage());
        }

        return SKIP_BODY;
    
    }

    public int doAfterBody() throws JspTagException {
      
        BodyContent body = getBodyContent();
      
        try {
    	
    		// Make sure we put anything in the output stream in the 
    		// body to the output stream of the JSP
        
            JspWriter out = body.getEnclosingWriter();
            out.print(body.getString());
        
        } catch(IOException ioe) {
            throw new JspTagException("Error in doAfterBody " + ioe);
        }
      
        return(SKIP_BODY); 
	
	}

}
