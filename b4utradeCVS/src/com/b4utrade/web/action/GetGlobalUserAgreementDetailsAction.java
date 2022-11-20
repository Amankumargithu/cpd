package com.b4utrade.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import com.google.gson.GsonBuilder;

public class GetGlobalUserAgreementDetailsAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(GetGlobalUserAgreementDetailsAction.class);
	private static final Gson gson = new GsonBuilder().serializeNulls().create();

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

		HashMap<String, String> responseMsg = new HashMap<>();
		response.setContentType("text/json");
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS, HEAD, PUT, POST");
		response.addHeader("Access-Control-Allow-Headers", "*");
		String userName = request.getParameter("USER_NAME");
		String companyId = request.getParameter("COMPANY_ID");
		if (userName == null || companyId == null)
			responseMsg.put("STATUS", "FAILURE");
		else {
			String query = "Select firstname, lastname,  addressline1, addressline2, city, state, zip, country, email, phone, occupation, subscriber_emp_name_add , subscriber_title, subscriber_emp_function,"
					+ " sign_date , NYSE_Q_A , NYSE_Q_O, NASDAQ_F, NYSE_F, OPRA_F, OTC_F, BATS_F, title, cancelled, reason, initial_activation_date, cancellation_date, last_reset_date, EmployerName, EmployerAddress, "
					+ " EmployerCity, EmployerState, EmployerZip, EmployerCountry, isSelfEmployed from USER_AGREEMENTS_GLOBAL where user_name = ? and company_id = ? ";
			try (Connection mysqlConn = MySQLConnectionProvider.createConnection("mysql1").getConnection();
					PreparedStatement preSmt = mysqlConn.prepareStatement(query);) {
				preSmt.setString(1, userName.toLowerCase());
				preSmt.setString(2, companyId);
				try (ResultSet rs = preSmt.executeQuery();) {
					if (rs.next()) {
						responseMsg.put("FIRSTNAME", rs.getString(1));
						responseMsg.put("LASTNAME", rs.getString(2));
						responseMsg.put("ADDRESS1", rs.getString(3));
						responseMsg.put("ADDRESS2", rs.getString(4));
						responseMsg.put("CITY", rs.getString(5));
						responseMsg.put("STATE", rs.getString(6));
						responseMsg.put("ZIP", rs.getString(7));
						responseMsg.put("COUNTRY", rs.getString(8));
						responseMsg.put("EMAIL", rs.getString(9));
						responseMsg.put("PHONE", rs.getString(10));
						responseMsg.put("OCCUPATION", rs.getString(11));
						responseMsg.put("SUBNAME", rs.getString(12));
						responseMsg.put("SUBTITLE", rs.getString(13));
						responseMsg.put("SUBFUNC", rs.getString(14));
						responseMsg.put("SIGNDATE", rs.getString(15));
						responseMsg.put("NYSEQA", rs.getString(16));
						responseMsg.put("NYSEQO", rs.getString(17));
						responseMsg.put("NASDAQ_F", rs.getString(18));
						responseMsg.put("NYSE_F", rs.getString(19));
						responseMsg.put("OPRA_F", rs.getString(20));
						responseMsg.put("OTC_F", rs.getString(21));
						responseMsg.put("BATS_F", rs.getString(22));
						responseMsg.put("TITLE", rs.getString(23));
						Boolean b = rs.getBoolean(24);
						if (b == null || !b)
							responseMsg.put("CANCELLED", "false");
						else
							responseMsg.put("CANCELLED", "true");
						responseMsg.put("REASON", rs.getString(25));
						responseMsg.put("Init_Activation_Date", rs.getString(26));
						responseMsg.put("Cancellation_Date", rs.getString(27));
						responseMsg.put("Last_Reset_Date", rs.getString(28));
						responseMsg.put("EmployerName", rs.getString(29));
						responseMsg.put("EmployerAddress", rs.getString(30));
						responseMsg.put("EmployerCity", rs.getString(31));
						responseMsg.put("EmployerState", rs.getString(32));
						responseMsg.put("EmployerZip", rs.getString(33));
						responseMsg.put("EmployerCountry", rs.getString(34));
						responseMsg.put("isSelfEmployed", rs.getString(35));
					}
				}
				responseMsg.put("USERNAME", userName);
			} catch (Exception e) {
				String msg = "Unable to retrieve User Agreement details from the database.";
				log.error("GetGlobalUserAgreementDetailsAction " + msg, e);
				return null;
			}
		}
		try (PrintWriter sos = response.getWriter();) {
			sos.write(gson.toJson(responseMsg));
			sos.flush();
		} catch (Exception e) {
			log.error("GetGlobalUserAgreementDetailsAction.execute() : servlet output stream encountered exception. ",
					e);
		}
		return null;
	}
}