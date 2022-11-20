/**
 * UserPortfolioDetailBean.java
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2004.  All rights reserved.
 * @version 1.0
 */

package com.b4utrade.bean;

import com.tacpoint.common.DefaultObject;


/**
 *   The user portfolio detail bean object holding detail portfolio data.
 */
public class UserPortfolioDetailBean extends DefaultObject
{
        private String  companyTicker = "";
        private String  companyName = "";
        private String  createDate = "";  
        private String  position = "";
        private String  share = "";
        private String  costPerShare = "";
        private String  transDate = "";


        /**
         *  Default constructor - does nothing.
         */
        public UserPortfolioDetailBean()
        {
           // do nothing
        }

        public void setCompanyTicker(String inTicker) {
            
            this.companyTicker = inTicker;
            
        }
        
        public String getCompanyTicker() {
            
            return companyTicker;
            
        }
        
        
        public void setCompanyName(String inName) {
            
            this.companyName = inName;
            
        }
        
        public String getCompanyName() {
            
            return companyName;
            
        }


        public void setCreateDate(String inDate) {
            
            this.createDate = inDate;
            
        }
        
        public String getCreateDate() {
            
            return createDate;
            
        }
        
        
        public void setPosition(String inPosition)
        {
            this.position = inPosition;
        }
        
        public String getPosition()
        {
            return position;
        }


        public void setShare(String inShare) 
        {
            this.share = inShare;
        }
        
        public String getShare()
        {
            return share;
        }
        
        
        public void setCostPerShare(String inCostPerShare)
        {
            this.costPerShare = inCostPerShare;
        }
        
        public String getCostPerShare()
        {
            return costPerShare;
        }
        
        
        public void setTransDate(String inTransDate)
        {
            this.transDate = inTransDate;
        }
        
        public String getTransDate()
        {
            return transDate;
        }           



}
