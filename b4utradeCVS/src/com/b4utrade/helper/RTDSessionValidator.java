package com.b4utrade.helper;

import javax.servlet.http.Cookie;

import com.b4utrade.bean.UserLoginStatusBean;
import com.tacpoint.util.Environment;

public class RTDSessionValidator {

	public static UserLoginStatusBean handleCheckUserSession(Cookie[] cookies) {
		String sessionId = null;	
		UserLoginStatusBean ulsb = new  UserLoginStatusBean();
		
		if(cookies != null){
			for(Cookie cookie : cookies){
				String name = cookie.getName();
			    if(name.equals((String)Environment.get("SESSION_COOKIE"))){
			    	sessionId = cookie.getValue();
			    }
			}
		}
		
		System.out.println("Session id is : "+sessionId);
		if(sessionId == null)
		{
			ulsb.setStatusSessionCheckFailed();
			return ulsb;
		}
		else{
			try {
				SingleLoginHelper slh = new SingleLoginHelper();
				slh.setSessionID(sessionId);
				if (slh.doesEQPlusSessionExist()) {
					ulsb.setUserID(slh.getUserID());
					ulsb.setStatusLoginSuccess();
					ulsb.setMessage("Session Authenticated");
				} else {
					ulsb.setStatusSessionCheckFailed();
					ulsb.setMessage("Invaid Session");
				}
				return ulsb;
			} catch (Exception e) {
				System.out.println("exception : "+e.getMessage());
				e.printStackTrace();
				ulsb.setStatusSessionCheckFailed();
				return ulsb;
			}
		}
	}
}