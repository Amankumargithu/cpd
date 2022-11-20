package com.b4utrade.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

import com.b4utrade.mysql.MySQLConnectionProvider;
import com.google.gson.Gson;

public class GetGlobalUserAgreementFlagAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(GetGlobalUserAgreementFlagAction.class);
	private static final Gson gson = new Gson();

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
			System.out.println(
					new Timestamp(System.currentTimeMillis()) + " AGREEMENTS FLAG ENTER " + userName + " " + companyId);
			try (Connection mysqlConn = MySQLConnectionProvider.createConnection("mysql1").getConnection();) {
				// Get agreements details to be filled
				String query = "Select  agreements from USER_AGREEMENT_ADMIN WHERE company_id = ? ";
				String agreements = "";
				try (PreparedStatement preSmt = mysqlConn.prepareStatement(query);) {
					preSmt.setString(1, companyId);
					try (ResultSet rs = preSmt.executeQuery();) {
						if (rs.next())
							agreements = rs.getString("agreements");
					}
				}
				String[] agreementArr = agreements.split(",");
				query = "Select  flag, NASDAQ_F, NYSE_F, OPRA_F, OTC_F, BATS_F, cancelled, reason from USER_AGREEMENTS_GLOBAL WHERE user_name = ? AND company_id = ? ";
				boolean flag = false;
				try (PreparedStatement preSmt = mysqlConn.prepareStatement(query);) {
					preSmt.setString(1, userName.toLowerCase());
					preSmt.setString(2, companyId);
					try (ResultSet rs = preSmt.executeQuery();) {
						boolean cancelled = false;
						if (rs.next()) {
							flag = rs.getString("flag").equals("true");
							cancelled = rs.getBoolean("cancelled");
							if (cancelled) {
								flag = false;
								responseMsg.put("REASON", "Cancelled - " + rs.getString("reason"));
							} else {
								for (String s : agreementArr) {
									switch (s) {
									case "NASDAQ":
										flag = flag && rs.getString("NASDAQ_F").equals("true");
										break;
									case "NYSE":
										flag = flag && rs.getString("NYSE_F").equals("true");
										break;
									case "OPRA":
										flag = flag && rs.getString("OPRA_F").equals("true");
										break;
									case "OTC":
										flag = flag && rs.getString("OTC_F").equals("true");
										break;
									case "BATS":
										flag = flag && rs.getString("BATS_F").equals("true");
										break;
									default:
										break;
									}
								}
							}
						}
					}
				}
				responseMsg.put("Username", userName);
				if (flag)
					responseMsg.put("Agreement", "SIGNED");
				else
					responseMsg.put("Agreement", "UNSIGNED");
				responseMsg.put("STATUS", "SUCCESS");
				responseMsg.put("COMPANY_ID", companyId);
				System.out.println(new Timestamp(System.currentTimeMillis()) + " AGREEMENTS FLAG EXIT " + userName + " "
						+ companyId);
			} catch (Exception e) {
				responseMsg.put("STATUS", "FAILURE");
				String msg = "Unable to get global User Agreement flag to the database.";
				log.error("GetGlobalUserAgreementFlagAction " + msg, e);
			}
		}
		try (PrintWriter sos = response.getWriter();) {
			sos.write(gson.toJson(responseMsg));
			sos.flush();
		} catch (Exception e) {
			log.error("GetGlobalUserAgreementFlagAction.execute() : servlet output stream encountered exception. ", e);
		}
		return (null);
	}
}