package com.b4utrade.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

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

public class GetAllGlobalAgreementCompanyAction extends B4UTradeDefaultAction{

	static Log log = LogFactory.getLog(GetAllGlobalAgreementCompanyAction.class);

	public ActionForward execute(ActionMapping mapping,ActionForm form,HttpServletRequest request,
			HttpServletResponse response)throws IOException, ServletException {
		Gson gson = new Gson();
		ArrayList<String> responseMsg = new ArrayList<String>();
		try {
			response.setContentType("text/json");
			Connection mysqlConn = MySQLConnectionProvider.createConnection("mysql1").getConnection();
			String query = "Select distinct(company_name)  from USER_AGREEMENT_ADMIN";
			PreparedStatement preSmt= mysqlConn.prepareStatement(query);
			ResultSet rs = preSmt.executeQuery();
			while(rs.next()) {
				String username = rs.getString(1);
				responseMsg.add(username);
			}	
			preSmt.close();	
			mysqlConn.close();
		}
		catch(Exception e) {
			String msg = "Unable to get company name list from the database.";
			log.error("GetAllGlobalAgreementCompanyAction "+ msg , e);
			return null;
		}
		try(PrintWriter sos = response.getWriter();) {
			sos.write(gson.toJson(responseMsg));
			sos.flush();
		}
		catch (Exception e)
		{
			log.error("GetAllGlobalAgreementCompanyAction.execute() : servlet output stream encountered exception. ", e);
		}
		return (null);
	}
}