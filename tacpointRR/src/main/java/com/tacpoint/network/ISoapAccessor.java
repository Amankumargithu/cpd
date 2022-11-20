 package com.tacpoint.network;
 
 import com.tacpoint.exception.ApplicationException;
 import java.util.*;
 

 /** 
  * ISoapAccessor.java
  * <P>
  *   The interface for all ISoapAccessor which to prepare the data and
  *   to retrieve data from a soap service.
  * <P>
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Tacpoint Technologies, Inc.
  * @version 1.0
  * Date created: 02/14/2002
  * Date modified:
  * - 02/14/2002 Initial version
  *   The interface for all SoapAccessors.
  */
 
        
  public interface ISoapAccessor
  {
     
    /**
     *   Set the target object uri that the soap call will be used.
     *   @param String objecturi the target object uri the soap call is used.
     *
     */
     
    public void setTargetObjectURI(String objecturi);
    
    /**
     *   Set the method name that the soap call will be used.
     *   @param String methodName the method Name the soap call is used.
     *
     */
     
    public void setMethodName(String methodname);
    
      
    /**
     *   The EncodingStyleURI that the soap call will be used.
     *   @return String EncodingStyleURI the Encoding Style URI the soap call is used.
     *
     */
    /**
     *   To get the SoapMappingRegistry to use.
     *
     *  @return Object
     */
    public Object getSoapMappingRegistry();
    
    /**
     *   The targetObjectURI that the soap call will be used.
     *   @return String targetObjectURI the targetObjectURI the soap call is used.
     *
     */
     
    public String getTargetObjectURI();
    
    /**
     *   The method name that the soap call will be used.
     *   @return String methodName the method Name the soap call is used.
     *
     */
     
    public String getMethodName();
    
      
    /**
     *   The EncodingStyleURI that the soap call will be used.
     *   @return String EncodingStyleURI the Encoding Style URI the soap call is used.
     *
     */
     
    public String getEncodingStyleURI();
    
      
    /**
     *   The Parameters that the soap call will be used.
     *   @return Vector parameters the parameters the soap call is used.
     *
     */
     
    public Vector getParameters();

      
    /**
     *   Pass in the result object, and manipulated by this method.
     *   @param Object result  the result object
     */
     
    public void setResultData(Object result);



  }
  