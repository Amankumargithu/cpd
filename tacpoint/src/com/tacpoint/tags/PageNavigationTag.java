package com.tacpoint.tags;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import javax.servlet.http.*;

import com.tacpoint.util.*;
import com.tacpoint.exception.*;

/**
 * PageNavigationTag is used to provide JSP's tag generated page navigation
 * in the form of html href(s).
 *
 * The following is an example of how to use the tag within a jsp file:
 *
 * A) Import the .tld file:
 *
 *    <%@ taglib uri="PageNav.tld" prefix="j2ee" %>
 *
 * B) Add a form element used to hold the page navigation start index:
 *
 *    <input type="hidden" name="PAGE_NAV_START_INDEX" value="">
 *
 * C) Define an entry in environment.properties with the following name.  This
 *    is used to resolve the afforementioned form element name.  
 *
 *    PAGE_NAV_START_INDEX_NAME=PAGE_NAV_START_INDEX
 *
 * D) Format custom tag invokation:
 *    Please not that the fontClass "nt" must be defined and accessible to the html page.
 *
 *    <j2ee:pageNav 
 *         target="javascript:document.MY_FORM_NAME.submit()" 
 *         numResultsPerPage="10" 
 *         totalResults="90" 
 *         formName="MY_FORM_NAME"
 *         fontClass="nt"
 *         prevImageHTMLDefinition="<IMG SRC=\"prev.gif\" BORDER=\"0\" HSPACE=\"5\">"
 *         nextImageHTMLDefinition="<IMG SRC=\"next.gif\" BORDER=\"0\" HSPACE=\"5\">"
 *     />
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2001.  All rights reserved.
 * @version 1.0
 */
public class PageNavigationTag extends TagSupport {

  /**
   * The target url or form name for submittal
   */
  private String target;

  /**
   * The form name used for form based submittals
   */
  private String formName = null;

  /**
   * Number of results per page used in determining number of page links
   */
  private String numResultsPerPage;

  /**
   * Total number of results
   */
  private String totalResults;

  /**
   * The Font class to be used with the navigation results
   */
  private String fontClass;

  /**
   * The title to be displayed to the left of the navigation pages
   */
  private String navigationTitle;

  /**
   * The image path and name for the previous navigation link
   *
   * E.g, <IMG SRC="x.gif" BORDER="0" HSPACE="5"> 
   */
  private String prevImageHTMLDefinition;

  /**
   * The image path and name for the next navigation link
   */
  private String nextImageHTMLDefinition;

  /**
   * Set method for target
   *
   * @param String
   */
  public void setTarget(String target) {
     this.target = target;
  }

  /**
   * Set method for form name
   *
   * @param String
   */
  public void setFormName(String formName) {
     this.formName = formName;
  }

  /**
   * Set method for total number of results
   *
   * @param String
   */
  public void setTotalResults(String totalResults) {
     this.totalResults = totalResults;
  }

  /**
   * Set method for number of results per page
   *
   * @param String
   */
  public void setNumResultsPerPage(String numResultsPerPage) {
     this.numResultsPerPage = numResultsPerPage;
  }

  /**
   * Set method for font class
   *
   * @param String
   */
  public void setFontClass(String fontClass) {
     this.fontClass = fontClass;
  }

  /**
   * Set method for navigation title
   *
   * @param String
   */
  public void setNavigationTitle(String navigationTitle) {
     this.navigationTitle = navigationTitle;
  }

  /**
   * Set method for previous image html definition
   *
   * @param String
   */
  public void setPrevImageHTMLDefinition(String prevImageHTMLDefinition) {
     this.prevImageHTMLDefinition = prevImageHTMLDefinition;
  }

  /**
   * Set method for next image html definition
   *
   * @param String
   */
  public void setNextImageHTMLDefinition(String nextImageHTMLDefinition) {
     this.nextImageHTMLDefinition = nextImageHTMLDefinition;
  }

  /**
   * Called by the JSP to compute the html used for page navigation.  
   * page or the error page is requested.
   *
   * @return Boolean whether or not to include JSP body (false)
   */
  public int doStartTag() {

     String startIndexName = Environment.get("PAGE_NAV_START_INDEX_NAME");
  
     int startIndex = 0;
     int resultsPerPage = 0;
     int totResults = 0;

     try {
        startIndex = Integer.parseInt(pageContext.getRequest().getParameter(startIndexName));
        if (startIndex < 0) startIndex = 0;
     }
     catch (NumberFormatException nfe) {
        Logger.log("PageNavigationTag.doStartTag() - Unable to parse start index ["+
                   pageContext.getRequest().getParameter(startIndexName)+"].  Start index being set to 0");
     }

     try {
        resultsPerPage = Integer.parseInt(numResultsPerPage);
        if (resultsPerPage < 1) {
           Logger.log("PageNavigationTag.doStartTag() - Invalid results per page value ["+
                      numResultsPerPage+"]");
           return SKIP_BODY;
        }
     }
     catch (NumberFormatException nfe) {
        Logger.log("PageNavigationTag.doStartTag() - Unable to parse results per page ["+
                   numResultsPerPage+"]");
        return SKIP_BODY;
     }

     try {
        totResults = Integer.parseInt(totalResults);
     }
     catch (NumberFormatException nfe) {
        Logger.log("PageNavigationTag.doStartTag() - Unable to parse total results ["+
                   totalResults+"]",nfe);
        return SKIP_BODY;
     }

     if (navigationTitle == null) navigationTitle = "";

     boolean useFontClass = fontClass == null ? false : true;
     String begFont = "<font id=\""+fontClass+"\">";
     String endFont = "</font>";

     boolean isForm = formName == null ? false : true;

     String pageTargetBeg = "<a href=\"";
     String pageTargetEnd = "</a>"; 

     String  setAttributeJSBeg = "\" onClick=\"document."+formName+"."+startIndexName+".value='";
     String setAttributeJSEnd = "';";

     if (!isForm) {
        if (target.indexOf("?") > 0) {
           target+=startIndexName+"=";
        }
        else {
           target+="?"+startIndexName+"=";
        }
     }

     try {
        int numPages = totResults / resultsPerPage;
        if (totResults % resultsPerPage != 0) numPages++;

        if (numPages <= 1) return SKIP_BODY;

        int curPage = (startIndex+1) / resultsPerPage;

        // special handling occurs when resultsPerPage = 1...
        if (resultsPerPage == 1)
           curPage--;

        JspWriter out = pageContext.getOut();

        out.write("<table><tr>");

        //
        // set up prev image page navigation ...
        //
        out.write("<td valign = center width=\"1%\">");
        if (curPage != 0) {
           out.write(pageTargetBeg+target);
           if (!isForm)
              out.write(""+((curPage-1)*resultsPerPage));
           else
              out.write(setAttributeJSBeg+((curPage-1)*resultsPerPage)+setAttributeJSEnd);
           out.write("\">"+prevImageHTMLDefinition+pageTargetEnd);
           out.write("</td>");
        }
        else {
           out.write("&nbsp;</td>");
        }
        
        //
        // set up numeric page navigation...
        //
        out.write("<td valign=center align=center width=\"98%\">");
        if (useFontClass)
           out.write(begFont);
        out.write(navigationTitle);
        if (useFontClass)
           out.write(endFont);

        int index = 0;

        while (true) {
           if (index != curPage) {

              if (useFontClass)
                 out.write(begFont);

              out.write(pageTargetBeg+target);
              if (!isForm)
                 out.write(""+(index*resultsPerPage));
              else
                 out.write(setAttributeJSBeg+(index*resultsPerPage)+setAttributeJSEnd);
              out.write("\">"+(index+1)+pageTargetEnd);

              if (useFontClass)
                 out.write(endFont);

              out.write(" ");
           }
           else {
              out.write("<b>");
              if (useFontClass)
                 out.write(begFont);
              out.write(String.valueOf((index+1)));
              if (useFontClass)
                 out.write(endFont);
              out.write("</b> ");
           }
           index++;
           if (index >= numPages)
              break;
        } 
        out.write("</td>");

        //
        // set up next image page navigation...
        //
        out.write("<td valign = center width=\"1%\">");
        if ((curPage+1) < numPages) {
           out.write(pageTargetBeg+target);
           if (!isForm)
              out.write(""+((curPage+1)*resultsPerPage));
           else
              out.write(setAttributeJSBeg+((curPage+1)*resultsPerPage)+setAttributeJSEnd);
           out.write("\">"+nextImageHTMLDefinition+pageTargetEnd);
           out.write("</td>");
        }
        else {
           out.write("&nbsp;</td>");
        }

        out.write("</tr></table>");

     }
     catch (Exception e) {
        Logger.log("PageNavigationTag.doStartTag() - Unable to format page navigation html",e);
        return SKIP_BODY;
     }

     return SKIP_BODY;
  }
}
