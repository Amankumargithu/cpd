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

public class ReactivateGlobalUserAgreementAction extends B4UTradeDefaultAction{

	static Log log = LogFactory.getLog(ReactivateGlobalUserAgreementAction.class);

	public ActionForward execute(ActionMapping mapping,ActionForm form,HttpServletRequest request,
			HttpServletResponse response)throws IOException, ServletException {
		Gson gson = new Gson();
		HashMap<String, String> responseMsg = new HashMap<>();
		Connection mysqlConn =null;
		PreparedStatement preSmt=null;
		Connection warriorConn = null;
		String userName = request.getParameter("USER_NAME").toLowerCase();
		String companyId = request.getParameter("COMPANY_ID");
		String timestamp=new Timestamp(System.currentTimeMillis()).toString();
		try {
			response.setContentType("text/json");			
			if(userName == null || companyId == null)
				responseMsg.put("STATUS", "FAILURE");
		}
		catch(Exception e) {
			responseMsg.put("STATUS", "FAILURE");
			String msg = "Unable to reset global User Agreement flag in the database.";
			log.error("ReactivateGlobalUserAgreementAction "+msg, e);
		}
		try
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
			if(isAgreement)
			{
				query = "UPDATE USER_AGREEMENTS_GLOBAL set flag = 'true', NASDAQ_F = 'true', NYSE_F = 'true', OPRA_F = 'false', OTC_F = 'false', BATS_F = 'true' WHERE user_name = ? AND company_id = ? ";
				preSmt= mysqlConn.prepareStatement(query);
				preSmt.setString(1, userName.toLowerCase());
				preSmt.setString(2, companyId);
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
				preSmt.setString(3, timestamp);
				preSmt.setBoolean(4, true);
				preSmt.executeUpdate();
				preSmt.close();
				mysqlConn.close();
				mysqlConn = null;
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

			if(companyId != null && companyId.equals("WRT_TRD"))
			{

				query = null;
				preSmt = null;
				warriorConn = MySQLConnectionProvider.createConnection("warrior").getConnection();
				query = "Select UserID from Warrior.Users where Username = ?";
				preSmt= warriorConn.prepareStatement(query);
				preSmt.setString(1, userName);
				System.out.println(new Timestamp(System.currentTimeMillis()) + " WARRIOR " + query);
				rs = preSmt.executeQuery();
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
				warriorConn.close();
				warriorConn = null;
			}
		}
		catch(Exception e)
		{
			responseMsg.put("STATUS", "FAILURE");
			String msg = "Unable to add user in Network Appliance";
			log.error("ReactivateGlobalUserAgreementAction "+msg, e);			
		}
		finally
		{
			try {
				if(preSmt!=null)
					preSmt.close();
				if(mysqlConn!=null&&!mysqlConn.isClosed())
					mysqlConn.close();
				if(warriorConn!=null&&!warriorConn.isClosed())
					warriorConn.close();
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
			log.error("ReactivateGlobalUserAgreementAction.execute() : servlet output stream encountered exception. ", e);
		}
		return (null);
	}
}