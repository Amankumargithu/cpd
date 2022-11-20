package com.b4utrade.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;

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

public class ResetGlobalUserAgreementCancellationAction extends B4UTradeDefaultAction{

	static Log log = LogFactory.getLog(ResetGlobalUserAgreementCancellationAction.class);

	public ActionForward execute(ActionMapping mapping,ActionForm form,HttpServletRequest request,
			HttpServletResponse response)throws IOException, ServletException {
		Connection mysqlConn =null;
		PreparedStatement preSmt=null;
		Gson gson = new Gson();
		HashMap<String, String> responseMsg = new HashMap<>();
		String userName =null;
		String companyId = null;
		String timestamp=new Timestamp(System.currentTimeMillis()).toString();
		try {
			userName = request.getParameter("USER_NAME").toLowerCase();
			companyId = request.getParameter("COMPANY_ID");
			response.setContentType("text/json");			
			if(userName == null || companyId == null)
				responseMsg.put("STATUS", "FAILURE");
			else 			
			{
				mysqlConn = MySQLConnectionProvider.createConnection("mysql1").getConnection();
				boolean isAgreement = false;
				String query = "select flag from USER_AGREEMENTS_GLOBAL where user_name = '"+userName +"' AND COMPANY_ID = '"+companyId+"'";
				preSmt= mysqlConn.prepareStatement(query);
				ResultSet rs = preSmt.executeQuery();
				if(rs.next())
				{
					isAgreement = true;	
				}
				preSmt.execute();
				preSmt.close();
				if(isAgreement){
					query = "UPDATE USER_AGREEMENTS_GLOBAL set cancelled = false, reason = null , CANCELLATION_DATE = ? , LAST_RESET_DATE = ? WHERE user_name = ? AND company_id = ? ";
					preSmt= mysqlConn.prepareStatement(query);
					preSmt.setString(1,timestamp);
					preSmt.setString(2, timestamp);
					preSmt.setString(3, userName.toLowerCase());
					preSmt.setString(4, companyId);
					preSmt.executeUpdate();
					preSmt.close();
					StringBuffer sb = new StringBuffer();
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
					preSmt.setString(3,timestamp);
					preSmt.setBoolean(4, false);
					preSmt.executeUpdate();
					preSmt.close();
					responseMsg.put("Username", userName);
					responseMsg.put("STATUS", "SUCCESS");
					responseMsg.put("COMPANY_ID", companyId);
				}
				else
				{
					responseMsg.put("Username", userName);
					responseMsg.put("STATUS", "FAILURE");
					responseMsg.put("COMPANY_ID", companyId);
				}
			}
		}
		catch(Exception e) {
			responseMsg.put("STATUS", "FAILURE");
			String msg = "Unable to reset global User Agreement cancellation flag in the database.";
			log.error("ResetGlobalUserAgreementCancellationAction "+msg, e);
		}
		finally
		{
			try {
				if(preSmt!=null)
					preSmt.close();
				if(mysqlConn!=null&&!mysqlConn.isClosed())
					mysqlConn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(companyId != null && companyId.equals("WRT_TRD"))
		{ 
			Connection warriorConn = null;
			try
			{
				String query = null;
				preSmt = null;
				warriorConn = MySQLConnectionProvider.createConnection("warrior").getConnection();
				query = "Select UserID from Warrior.Users where Username = ?";
				preSmt= warriorConn.prepareStatement(query);
				preSmt.setString(1, userName);
				System.out.println(new Timestamp(System.currentTimeMillis()) + " WARRIOR " + query);
				ResultSet rs = preSmt.executeQuery();
				Integer userid = 0;
				if(rs != null && rs.next())
					userid = rs.getInt("UserID");
				preSmt.close();
				if(userid > 0)
				{
					query = "Select * from Warrior.Permissions where UserId = ?";
					preSmt= warriorConn.prepareStatement(query);
					preSmt.setInt(1, userid);
					System.out.println(new Timestamp(System.currentTimeMillis()) + " WARRIOR " + query);
					rs = preSmt.executeQuery();
					HashSet<Integer> productIds = new HashSet<Integer>();
					productIds.add(2);
					productIds.add(3);
					productIds.add(4);
					productIds.add(5);
					productIds.add(7);
					productIds.add(8);
					productIds.add(9);
					productIds.add(10);
					productIds.add(32);
					productIds.add(295);
					productIds.add(296);
					while(rs.next())
						productIds.remove(rs.getInt("ProductID"));
					preSmt.close();
					for(Integer i : productIds)
					{
						query = "Insert into Warrior.Permissions VALUES (?,?)";
						preSmt= warriorConn.prepareStatement(query);
						preSmt.setInt(1, userid);
						preSmt.setInt(2, i);
						System.out.println(new Timestamp(System.currentTimeMillis()) + " WARRIOR " + query);
						preSmt.executeUpdate();
						preSmt.close();
					}
					query = "UPDATE Warrior.Update SET UpdateTime = NOW()";
					preSmt= warriorConn.prepareStatement(query);
					System.out.println(new Timestamp(System.currentTimeMillis()) + " WARRIOR " + query);
					preSmt.executeUpdate();
					preSmt.close();
				}
				
			}
			catch(Exception e)
			{
				responseMsg.put("STATUS", "FAILURE");
				String msg = "Unable to add user in Network Appliance";
				log.error("ResetGlobalUserAgreementCancellationAction "+msg, e);			
			}
			finally
			{
				try {
					if(preSmt != null)
						preSmt.close();
					if(warriorConn != null && !warriorConn.isClosed())
						warriorConn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
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
}