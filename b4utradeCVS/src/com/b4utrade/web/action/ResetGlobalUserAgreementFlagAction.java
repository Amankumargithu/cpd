package com.b4utrade.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.mysql.KarmaDBManager;
import com.b4utrade.mysql.MySQLConnectionProvider;
import com.google.gson.Gson;
import com.tacpoint.util.Environment;

public class ResetGlobalUserAgreementFlagAction extends B4UTradeDefaultAction{

	static Log log = LogFactory.getLog(ResetGlobalUserAgreementFlagAction.class);

	public ActionForward execute(ActionMapping mapping,ActionForm form,HttpServletRequest request,
			HttpServletResponse response)throws IOException, ServletException {
		Gson gson = new Gson();
		HashMap<String, String> responseMsg = new HashMap<>();
		Connection mysqlConn=null;
		PreparedStatement preSmt=null;
		String userName = null;
		String companyId =null;
		String cancel =null;
		String reason = null;
		String timestamp = new Timestamp(System.currentTimeMillis()).toString();
		try {
			userName = request.getParameter("USER_NAME").toLowerCase();
			companyId = request.getParameter("COMPANY_ID");
			cancel = request.getParameter("cancel");
			reason = request.getParameter("reason");
			response.setContentType("text/json");			
			if(userName == null || companyId == null)
				responseMsg.put("STATUS", "FAILURE");
			else if("true".equals(cancel))
			{
				mysqlConn = MySQLConnectionProvider.createConnection("mysql1").getConnection();
				StringBuffer sb = new StringBuffer();
				String query = "select CANCELLED from USER_AGREEMENTS_GLOBAL where user_name = ? AND COMPANY_ID = ?";
				preSmt= mysqlConn.prepareStatement(query);
				preSmt.setString(1, userName.toLowerCase());
				preSmt.setString(2, companyId);
				ResultSet rs = preSmt.executeQuery();
				boolean flag = false;
				if(rs.next())
					flag = rs.getBoolean("CANCELLED");
				preSmt.close();
				if(!flag)
				{
					query = "UPDATE USER_AGREEMENTS_GLOBAL set cancelled = true, reason = ?, CANCELLATION_DATE = ? , LAST_RESET_DATE = ? WHERE user_name = ? AND company_id = ? ";
					preSmt= mysqlConn.prepareStatement(query);
					preSmt.setString(1, reason);
					preSmt.setString(2, timestamp);
					preSmt.setString(3, timestamp);
					preSmt.setString(4, userName.toLowerCase());
					preSmt.setString(5, companyId);
					preSmt.executeUpdate();
					preSmt.close();
					sb = new StringBuffer();
					sb.append("insert into USER_AGREEMENTS_ACTIVATION (");
					sb.append("	USER_NAME,");
					sb.append("	COMPANY_ID,");
					sb.append("	DATE,");
					sb.append("	ACTIVATION_DATE) ");
					sb.append("values (?,?,?,?) ");
					query = sb.toString();
					preSmt= mysqlConn.prepareStatement(query);
					preSmt.setString(1, userName);
					preSmt.setString(2, companyId);
					preSmt.setString(3, timestamp);
					preSmt.setBoolean(4, false);
					preSmt.executeUpdate();				
					preSmt.close();
				}
				responseMsg.put("Username", userName);
				responseMsg.put("STATUS", "SUCCESS");
				responseMsg.put("COMPANY_ID", companyId);	
			}
			else
			{   
				StringBuffer sb = new StringBuffer();
				String query = "select flag from USER_AGREEMENTS_GLOBAL where user_name = ? AND COMPANY_ID = ?";
				mysqlConn = MySQLConnectionProvider.createConnection("mysql1").getConnection();
				preSmt= mysqlConn.prepareStatement(query);
				preSmt.setString(1, userName.toLowerCase());
				preSmt.setString(2, companyId);
				ResultSet rs = preSmt.executeQuery();
				boolean flag = false;
				if(rs.next())
					flag = rs.getString("flag").equals("true");
				preSmt.close();
				if(flag)
				{
					query = "UPDATE USER_AGREEMENTS_GLOBAL set flag = 'false', NASDAQ_F = 'false', NYSE_F = 'false', OPRA_F = 'false', OTC_F = 'false', BATS_F = 'false', LAST_RESET_DATE = ? WHERE user_name = ? AND company_id = ? ";
					preSmt= mysqlConn.prepareStatement(query);
					preSmt.setString(1, timestamp);
					preSmt.setString(2, userName.toLowerCase());
					preSmt.setString(3, companyId);
					preSmt.executeUpdate();
					preSmt.close();
					sb = new StringBuffer();
					sb.append("insert into USER_AGREEMENTS_ACTIVATION (");
					sb.append("	USER_NAME,");
					sb.append("	COMPANY_ID,");
					sb.append("	DATE,");
					sb.append("	ACTIVATION_DATE) ");
					sb.append("values (?,?,?,?) ");
					query = sb.toString();
					preSmt= mysqlConn.prepareStatement(query);
					preSmt.setString(1, userName);
					preSmt.setString(2, companyId);
					preSmt.setString(3, timestamp);
					preSmt.setBoolean(4, false);
					preSmt.executeUpdate();				
					preSmt.close();
				}
				responseMsg.put("Username", userName);
				responseMsg.put("STATUS", "SUCCESS");
				responseMsg.put("COMPANY_ID", companyId);	
			}			
		}
		catch(Exception e) {
			responseMsg.put("STATUS", "FAILURE");
			String msg = "Unable to reset global User Agreement flag in the database.";
			log.error("ResetGlobalUserAgreementFlagAction "+msg, e);
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
		if(companyId != null && companyId.equals("BLK_BOX"))
		{
			int clientUserId = getClientId(userName);
			cancelUser(clientUserId);
		}
		try(PrintWriter sos = response.getWriter();) {
			sos.write(gson.toJson(responseMsg));
			sos.flush();
		}
		catch (Exception e)
		{
			log.error("ResetGlobalUserAgreementFlagAction.execute() : servlet output stream encountered exception. ", e);
		}
		return (null);
	}
	
	private int getClientId(String username)
	{
		int clientUserId = 0;
		try{
			Connection conn = null;
			PreparedStatement stmt = null;
			String q = "SELECT id, internalId FROM aspnetusers WHERE UserName = ?";
			String token = null;
			conn = KarmaDBManager.getConnection();
			stmt = conn.prepareStatement(q);
			stmt.setString(1, username);
			String s = stmt.toString();
			s=s.substring(s.indexOf(":")+1, s.length());
			System.out.println(s);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				clientUserId = rs.getInt("internalId");
				token = rs.getString("id");
			}
			System.out.println("user_id "+clientUserId +"  token "+token);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			KarmaDBManager.closeConnection();
		}
		return clientUserId;
	}
	
	private void cancelUser(int clientUserId)
	{

		String qssDomain = Environment.get("QSS4_DOMAIN");
		if(qssDomain == null)
			qssDomain = "https://www5.quodd.com/vor/quodd/api";		
		try{
			String reactivateUserUrl = qssDomain + "/cid/"+clientUserId+"/user/cancel";
			URL obj = new URL(reactivateUserUrl);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			//add reuqest header
			con.setRequestMethod("GET");
			con.setRequestProperty("Authorization", "Basic YmxhY2tib3g6b25kZW1hbmRibGFja2JveA==");
			// Send post request
			con.setDoOutput(true);
			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + reactivateUserUrl);
			System.out.println("Response Code : " + responseCode);			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	
	}
}