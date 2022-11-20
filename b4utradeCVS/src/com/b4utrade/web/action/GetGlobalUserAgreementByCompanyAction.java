package com.b4utrade.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.mysql.MySQLConnectionProvider;
import com.google.gson.Gson;

public class GetGlobalUserAgreementByCompanyAction extends B4UTradeDefaultAction{

	static Log log = LogFactory.getLog(GetGlobalUserAgreementByCompanyAction.class);

	public ActionForward execute(ActionMapping mapping,ActionForm form,HttpServletRequest request,
			HttpServletResponse response)throws IOException, ServletException {
		Connection mysqlConn=null;
		PreparedStatement preSmt=null;
		Gson gson = new Gson();
		HashMap responseMsg = new HashMap<>();
		String companyId;
		try {
			  response.setContentType("text/json");
		      String company = request.getParameter("COMPANY");
		      companyId = null;
		      mysqlConn = MySQLConnectionProvider.createConnection("mysql1").getConnection();
		      
		      String query = "Select agreements, company_id from USER_AGREEMENT_ADMIN where company_name = ?";
		      preSmt = mysqlConn.prepareStatement(query);
		      preSmt.setString(1, company);
		      
		      ResultSet rs = preSmt.executeQuery();
		      
		      if (rs.next()) 
		      {
		        responseMsg.put("STATUS", "SUCCESS");
		        responseMsg.put("COMPANY_NAME", company);
		        responseMsg.put("AGREEMENTS", rs.getString("agreements"));
		        responseMsg.put("COMPANY_ID", rs.getString("company_id"));
		        companyId = rs.getString("company_id");
		      }
		      preSmt.close();
		      
		      query = "SELECT  AG.user_name, AG.firstname, AG.lastname, AG.flag, AG.cancelled , AG.initial_activation_date FROM USER_AGREEMENTS_GLOBAL AS AG WHERE AG.company_id =? GROUP BY AG.flag DESC, AG.user_name";
		      preSmt = mysqlConn.prepareStatement(query);
		      preSmt.setString(1, companyId);
		      rs = preSmt.executeQuery();
		      
		      ArrayList<HashMap<String,String>> userList = new ArrayList<HashMap<String,String>>();
		      ArrayList<HashMap<String,String>> cancelledList = new ArrayList<HashMap<String,String>>();

		      SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		      while (rs.next()) {
		        HashMap<String,String> userInfo = new HashMap<String,String>();
		        userInfo.put("USER_NAME", rs.getString("user_name"));
		        userInfo.put("FIRSTNAME", rs.getString("firstname"));
		        userInfo.put("LASTNAME", rs.getString("lastname"));
		        userInfo.put("INITIAL_ACTIVATION_DATE", dateformat.format(rs.getTimestamp("initial_activation_date")));
		        int cancelled = rs.getInt("cancelled");
		        if (cancelled == 1)
		        {
		          userInfo.put("STATUS", "CANCELLED");
		          userInfo.put("CANCELLED", "1");
		          cancelledList.add(userInfo);
		        }
		        else
		        {
		          String flag = rs.getString("flag");
		          if (flag.equalsIgnoreCase("true")) {
		            userInfo.put("STATUS", "ACTIVE");
		          }
		          else {
		            userInfo.put("STATUS", "DEACTIVE");
		          }
		          userInfo.put("CANCELLED", "0");
		          userList.add(userInfo);
		        }
		      }
		      preSmt.close();
		      userList.addAll(cancelledList);
		      responseMsg.put("USER_LIST", userList);
		      }
		catch(Exception e) {
			log.error("GetGlobalUserAgreementByCompanyAction ", e);
			return null;
		}
		finally
		{
			try {
				if(preSmt != null)
					preSmt.close();
				if(mysqlConn != null && !mysqlConn.isClosed())
					mysqlConn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}
		try(PrintWriter sos = response.getWriter();) {
			sos.write(gson.toJson(responseMsg));
			sos.flush();
		}
		catch (Exception e)
		{
			log.error("GetGlobalUserAgreementByCompanyAction.execute() : servlet output stream encountered exception. ", e);
		}
		return (null);
	}
}
