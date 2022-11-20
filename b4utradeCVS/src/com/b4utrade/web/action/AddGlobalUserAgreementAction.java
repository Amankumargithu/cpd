package com.b4utrade.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

public class AddGlobalUserAgreementAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(AddGlobalUserAgreementAction.class);
	private static final Gson gson = new Gson();

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

		HashMap<String, String> responseMsg = new HashMap<>();
		String errorMsg = null;
		response.setContentType("text/json");
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS, HEAD, PUT, POST");
		response.addHeader("Access-Control-Allow-Headers", "*");
		String dataInputObj = request.getParameter("AGREEMENT_BEAN");
		dataInputObj = URLDecoder.decode(dataInputObj, "UTF-8");
		String timestamp = new Timestamp(System.currentTimeMillis()).toString();
		DateFormat dateFormat = new SimpleDateFormat("dd MMMMM, yyyy");
		HashMap<String, String> map = null;
		String companyId = "";
		try {
			map = gson.fromJson(dataInputObj, HashMap.class);
			map.put("signDate", dateFormat.format(new Date()));
		} catch (Exception e) {
			responseMsg.put("STATUS", "FAILURE");
			errorMsg = "Unable to parse JSON input parameter";
			responseMsg.put("REASON", errorMsg);
			log.error("ERROR - Agreement " + dataInputObj + " " + e.getMessage(), e);
			writeMessage(response, gson.toJson(responseMsg));
			return null;
		}
		companyId = map.get("companyName");
		try (Connection mysqlConn = MySQLConnectionProvider.createConnection("mysql1").getConnection();) {
			StringBuilder sb = new StringBuilder();
			boolean isAgreement = false;
			boolean isActive = false;
			String query = "select flag from USER_AGREEMENTS_GLOBAL where user_name = '"
					+ map.get("userName").toLowerCase() + "' AND COMPANY_ID = '" + map.get("companyName") + "'";
			log.info(" AGREEMENTS " + query);
			try (PreparedStatement preSmt = mysqlConn.prepareStatement(query); ResultSet rs = preSmt.executeQuery();) {
				if (rs.next()) {
					isAgreement = true;
					isActive = rs.getString("flag").equals("true");
				}
			}
			if (!isAgreement) {
				sb.append("insert into USER_AGREEMENTS_GLOBAL (");
				sb.append("	USER_NAME,");
				sb.append("	firstName,");
				sb.append("	lastName,");
				sb.append("	addressLine1,");
				sb.append("	addressLine2,");
				sb.append("	city,");
				sb.append("	state,");
				sb.append("	zip,");
				sb.append("	country,");
				sb.append("	email,");
				sb.append("	phone,");
				sb.append("	title,");
				sb.append("	occupation,");
				sb.append("	subscriber_emp_name_Add,");
				sb.append("	subscriber_title,");
				sb.append("	subscriber_emp_function,");
				sb.append(" sign_date,");
				sb.append("	COMPANY_ID,");
				sb.append("	NYSE_Q_A,");
				sb.append("	NYSE_Q_O,");
				sb.append("	NASDAQ_F,");
				sb.append("	NYSE_F,");
				sb.append("	OPRA_F,");
				sb.append("	OTC_F,");
				sb.append("	BATS_F,");
				sb.append("	CANCELLED,");
				sb.append("	FLAG,");
				sb.append("	INITIAL_ACTIVATION_DATE,");
				sb.append("	EmployerName,");
				sb.append("	EmployerAddress,");
				sb.append("	EmployerCity,");
				sb.append("	EmployerState,");
				sb.append("	EmployerZip,");
				sb.append("	EmployerCountry,");
				sb.append("	isSelfEmployed) ");
				sb.append("values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ");
				query = sb.toString();
				try (PreparedStatement preSmt = mysqlConn.prepareStatement(query);) {
					preSmt.setString(1, map.get("userName").toLowerCase());
					preSmt.setString(2, map.get("firstName"));
					preSmt.setString(3, map.get("lastName"));
					preSmt.setString(4, map.get("addressLine1"));
					preSmt.setString(5, map.get("addressLine2"));
					preSmt.setString(6, map.get("city"));
					preSmt.setString(7, map.get("state"));
					preSmt.setString(8, map.get("zip"));
					preSmt.setString(9, map.get("country"));
					preSmt.setString(10, map.get("email"));
					preSmt.setString(11, map.get("number"));
					preSmt.setString(12, map.get("title"));
					preSmt.setString(13, map.get("occupation"));
					preSmt.setString(14, map.get("subscriberEmployeeNameAddress"));
					preSmt.setString(15, map.get("subscriberTitle"));
					preSmt.setString(16, map.get("subscriberEmploymentFunction"));
					preSmt.setString(17, map.get("signDate"));
					preSmt.setString(18, map.get("companyName"));
					preSmt.setString(19, map.get("questionA"));
					preSmt.setString(20, map.get("questionOther"));
					preSmt.setString(21, map.get("NASDAQ_F"));
					preSmt.setString(22, map.get("NYSE_F"));
					preSmt.setString(23, "true"); // for Karma
					preSmt.setString(24, map.get("OTC_F"));
					preSmt.setString(25, map.get("BATS_F"));
					preSmt.setBoolean(26, false);
					preSmt.setString(27, "true");
					preSmt.setString(28, timestamp);
					preSmt.setString(29, map.get("EmployerName"));
					preSmt.setString(30, map.get("EmployerAddress"));
					preSmt.setString(31, map.get("EmployerCity"));
					preSmt.setString(32, map.get("EmployerState"));
					preSmt.setString(33, map.get("EmployerZip"));
					preSmt.setString(34, map.get("EmployerCountry"));
					preSmt.setString(35, map.get("isSelfEmployed"));
					System.out.println(new Timestamp(System.currentTimeMillis()) + " AGREEMENTS " + query + " "
							+ map.get("userName").toLowerCase() + " " + companyId);
					preSmt.executeUpdate();
				}
				sb = new StringBuilder();
				sb.append("insert into USER_AGREEMENTS_ACTIVATION (");
				sb.append("	USER_NAME,");
				sb.append("	COMPANY_ID,");
				sb.append("	DATE,");
				sb.append("	ACTIVATION_DATE) ");
				sb.append("values (?,?,?,?) ");
				query = sb.toString();
				try (PreparedStatement preSmt = mysqlConn.prepareStatement(query);) {
					preSmt.setString(1, map.get("userName").toLowerCase());
					preSmt.setString(2, map.get("companyName"));
					preSmt.setString(3, timestamp);
					preSmt.setBoolean(4, true);
					preSmt.executeUpdate();
				}
			} else {
				if (!isActive) {
					sb.append("UPDATE USER_AGREEMENTS_GLOBAL set ");
					sb.append("	USER_NAME = ?,");
					sb.append("	firstName = ?,");
					sb.append("	lastName = ?,");
					sb.append("	addressLine1 = ?,");
					sb.append("	addressLine2 = ?,");
					sb.append("	city = ?,");
					sb.append("	state = ?,");
					sb.append("	zip = ?,");
					sb.append("	country = ?,");
					sb.append("	email  = ?,");
					sb.append("	phone = ?,");
					sb.append("	title = ?,");
					sb.append("	occupation = ?,");
					sb.append("	subscriber_emp_name_Add = ?,");
					sb.append("	subscriber_title = ?,");
					sb.append("	subscriber_emp_function = ?,");
					sb.append(" sign_date = ? ,");
					sb.append("	COMPANY_ID = ?,");
					sb.append("	NYSE_Q_A  = ?,");
					sb.append("	NYSE_Q_O = ? ,");
					sb.append("	NASDAQ_F = ?,");
					sb.append("	NYSE_F = ?,");
					sb.append("	OPRA_F = ?,");
					sb.append("	OTC_F = ? ,");
					sb.append("	BATS_F = ?,");
					sb.append("	CANCELLED = ?,");
					sb.append("	FLAG = ?,");
					sb.append("	EmployerName = ?,");
					sb.append("	EmployerAddress = ?,");
					sb.append("	EmployerCity = ?,");
					sb.append("	EmployerState = ?,");
					sb.append("	EmployerZip = ?,");
					sb.append("	EmployerCountry = ?,");
					sb.append("	isSelfEmployed = ?,");
					sb.append("	LAST_RESET_DATE = NULL where company_id = ? AND user_name = ?");
					query = sb.toString();

					try (PreparedStatement preSmt = mysqlConn.prepareStatement(query);) {
						preSmt.setString(1, map.get("userName").toLowerCase());
						preSmt.setString(2, map.get("firstName"));
						preSmt.setString(3, map.get("lastName"));
						preSmt.setString(4, map.get("addressLine1"));
						preSmt.setString(5, map.get("addressLine2"));
						preSmt.setString(6, map.get("city"));
						preSmt.setString(7, map.get("state"));
						preSmt.setString(8, map.get("zip"));
						preSmt.setString(9, map.get("country"));
						preSmt.setString(10, map.get("email"));
						preSmt.setString(11, map.get("number"));
						preSmt.setString(12, map.get("title"));
						preSmt.setString(13, map.get("occupation"));
						preSmt.setString(14, map.get("subscriberEmployeeNameAddress"));
						preSmt.setString(15, map.get("subscriberTitle"));
						preSmt.setString(16, map.get("subscriberEmploymentFunction"));
						preSmt.setString(17, map.get("signDate"));
						preSmt.setString(18, map.get("companyName"));
						preSmt.setString(19, map.get("questionA"));
						preSmt.setString(20, map.get("questionOther"));
						preSmt.setString(21, map.get("NASDAQ_F"));
						preSmt.setString(22, map.get("NYSE_F"));
						preSmt.setString(23, "true");
						preSmt.setString(24, map.get("OTC_F"));
						preSmt.setString(25, map.get("BATS_F"));
						preSmt.setBoolean(26, false);
						preSmt.setString(27, "true");
						preSmt.setString(28, map.get("EmployerName"));
						preSmt.setString(29, map.get("EmployerAddress"));
						preSmt.setString(30, map.get("EmployerCity"));
						preSmt.setString(31, map.get("EmployerState"));
						preSmt.setString(32, map.get("EmployerZip"));
						preSmt.setString(33, map.get("EmployerCountry"));
						preSmt.setString(34, map.get("isSelfEmployed"));
						preSmt.setString(35, map.get("companyName"));
						preSmt.setString(36, map.get("userName").toLowerCase());
						System.out.println(new Timestamp(System.currentTimeMillis()) + " AGREEMENTS " + query + " "
								+ map.get("userName").toLowerCase() + " " + companyId);
						preSmt.executeUpdate();
					}
					sb = new StringBuilder();
					sb.append("insert into USER_AGREEMENTS_ACTIVATION (");
					sb.append("	USER_NAME,");
					sb.append("	COMPANY_ID,");
					sb.append("	DATE,");
					sb.append("	ACTIVATION_DATE) ");
					sb.append("values (?,?,?,?) ");
					query = sb.toString();
					try (PreparedStatement preSmt = mysqlConn.prepareStatement(query);) {
						preSmt.setString(1, map.get("userName").toLowerCase());
						preSmt.setString(2, map.get("companyName"));
						preSmt.setString(3, timestamp);
						preSmt.setBoolean(4, true);
						preSmt.executeUpdate();
					}
				}
			}
			responseMsg.put("STATUS", "SUCCESS");
		} catch (Exception e) {
			responseMsg.put("STATUS", "FAILURE");
			errorMsg = "Unable to insert agreement to the database. " + e.getMessage();
			responseMsg.put("REASON", errorMsg);
			log.error("AddGlobalUserAgreementAction " + errorMsg, e);
			writeMessage(response, gson.toJson(responseMsg));
			return null;
		}
		responseMsg.put("REASON", errorMsg);
		writeMessage(response, gson.toJson(responseMsg));
		return null;
	}

	public void writeMessage(HttpServletResponse response, String msg) {
		try (PrintWriter sos = response.getWriter();) {
			sos.write(msg);
			sos.flush();
		} catch (Exception e) {
			log.error("AddUserAgreementAction.execute() : servlet output stream encountered exception. ", e);
		}
	}

}