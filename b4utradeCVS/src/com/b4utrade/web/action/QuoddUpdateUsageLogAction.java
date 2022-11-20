package com.b4utrade.web.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.helper.ConsoleTimestampLogger;
import com.b4utrade.mysql.MySQLConnectionProvider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class QuoddUpdateUsageLogAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddUpdateUsageLogAction.class);
	private static final Gson gson = new Gson();
	private boolean insertFlag = false;
	private String updateQuery = null;
	private String insertQuery = null;

	/**
	 * Process the specified HTTP request, and create the corresponding HTTP
	 * response (or forward to another web component that will create it). Return an
	 * <code>ActionForward</code> instance describing where and how control should
	 * be forwarded, or <code>null</code> if the response has already been
	 * completed.
	 * 
	 * @param mapping    The ActionMapping used to select this instance
	 * @param actionForm The optional ActionForm bean for this request (if any)
	 * @param request    The HTTP request we are processing
	 * @exception IOException      if an input/output error occurs
	 * @exception ServletException if a servlet exception occurs
	 */

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		doCheckUser = false;
		doCheckReferalPartner = false;
		ActionForward forward = super.execute(mapping, form, request, response);
		if (forward != null)
			return forward;

		try {
			response.setContentType("application/json");
			response.addHeader("Access-Control-Allow-Origin", "*");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (InputStream inputStream = request.getInputStream();) {
				int bytesRead = -1;
				byte buffer[] = new byte[1024];
				while ((bytesRead = inputStream.read(buffer)) != -1)
					baos.write(buffer, 0, bytesRead);
			} catch (Exception e) {
				log.error("Error while reading the input Stream (usage logs of user )", e);
			}

			String message = null;
			int upsertCount = 0;
			String requestBody = new String(baos.toByteArray());
			baos.close();
			if (requestBody == null || requestBody.isEmpty()) {
				message = "Request Body is null";
			} else {
				Vector<HashMap<String, String>> listOfLogs = gson.fromJson(requestBody,
						new TypeToken<Vector<HashMap<String, String>>>() {
						}.getType());
				int vectorSize = listOfLogs.size();
				if (vectorSize > 0) {
					Enumeration<HashMap<String, String>> enumeration = listOfLogs.elements();
					String type = null;
					String userId = null;
					String accessDate = null;
					String accessTime = null;
					String env = null;
					String device = null;
					String deviceId = null;
					String version = null;
					log.info("Number of record or queries in vector are : " + vectorSize);
					while (enumeration.hasMoreElements()) {
						HashMap<String, String> map = enumeration.nextElement();
						type = map.get("type");
						if (type == null || type.isEmpty()) {
							log.error("user usage log type is null in map or record");
							continue;
						}
						userId = map.get("userId");
						accessDate = map.get("accessDate");
						accessTime = map.get("accessTime");
						env = map.get("env");
						device = map.get("device");
						deviceId = map.get("deviceId");
						version = map.get("version");

						ConsoleTimestampLogger.println("Going to update the Usage Log of User " + userId);
						try (Connection con = MySQLConnectionProvider.createConnection("mysql1").getConnection();) {
							fetchSqlUpdateQuery(type);
							try (PreparedStatement preSmt = con.prepareStatement(updateQuery);) {
								preSmt.setString(1, accessTime);
								preSmt.setString(2, userId);
								preSmt.setString(3, accessDate);
								preSmt.setString(4, env);
								preSmt.setString(5, device);
								preSmt.setString(6, deviceId);
								preSmt.setString(7, version);
								int i = preSmt.executeUpdate();
								if (i > 0) {
									log.info("Succesfully Updated logs of User with userId : " + userId);
									upsertCount++;
									insertFlag = false;
								} else {
									log.info("Updation failed of user logs of User with userId : " + userId
											+ " going to insert then into db");
									insertFlag = true;
								}
							} catch (Exception e) {
								log.error("Error while updating the user usage logs .", e);
								insertFlag = true;
							}
							if (insertFlag) {
								ConsoleTimestampLogger.println("Going to insert the Usage Log of User : " + userId
										+ "  as update failed or there are no record to update.");
								fetchSqlInsertQuery(type);
								try (PreparedStatement preStat = con.prepareStatement(insertQuery);) {
									preStat.setString(1, userId);
									preStat.setString(2, accessDate);
									preStat.setString(3, accessTime);
									preStat.setString(4, env);
									preStat.setString(5, device);
									preStat.setString(6, deviceId);
									preStat.setString(7, version);
									int i = preStat.executeUpdate();
									if (i > 0) {
										upsertCount++;
										log.info("Succesfully inserted logs of User with userId : " + userId);
									} else {
										log.info("insertion failed of user logs of User with userId : " + userId);
									}
								} catch (Exception e) {
									log.error("Error while inserting the user usage logs .", e);
									continue;
								}
							}
						} catch (Exception e) {
							log.error(
									"QuoddUpdateUsageLogAction.execute():encountered an error while insertion/updation. ",
									e);
							continue;
						}
					}
					message = upsertCount + " Upsert Successfully and " + (vectorSize - upsertCount) + " upsert Failed";
				} else
					message = "usage logs are null in request body / vector";

			}
			try (ServletOutputStream sos = response.getOutputStream()) {
				sos.write(message.getBytes());
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddUpdateUsageLogAction.execute(): exception while sending response.", e);
			}
		} catch (Exception e) {
			String msg = "QuoddUpdateUsageLogAction.execute() : Unable to retrieve data.";
			log.error(msg, e);
		}
		return null;
	}

	private void fetchSqlUpdateQuery(String type) {
		switch (type) {
		case "1":
			updateQuery = "UPDATE USER_USAGE_LOG SET LOGIN = LOGIN + 1, LAST_MODIFIED = ? WHERE USER_ID = ? AND ACCESS_DATE = ?"
					+ "AND ENVIRONMENT = ? AND DEVICE = ? AND DEVICEID = ? AND VERSION = ?";
			break;
		case "2":
			updateQuery = "UPDATE USER_USAGE_LOG SET LOGOUT = LOGOUT + 1, LAST_MODIFIED = ? WHERE USER_ID = ? AND ACCESS_DATE = ?"
					+ "AND ENVIRONMENT = ? AND DEVICE = ? AND DEVICEID = ? AND VERSION = ?";
			break;
		case "3":
			updateQuery = "UPDATE USER_USAGE_LOG SET ENTITLEMENT = ENTITLEMENT + 1, LAST_MODIFIED = ? WHERE USER_ID = ? AND ACCESS_DATE = ?"
					+ "AND ENVIRONMENT = ? AND DEVICE = ? AND DEVICEID = ? AND VERSION = ?";
			break;
		case "4":
			updateQuery = "UPDATE USER_USAGE_LOG SET SNAP = SNAP + 1, LAST_MODIFIED = ? WHERE USER_ID = ? AND ACCESS_DATE = ?"
					+ "AND ENVIRONMENT = ? AND DEVICE = ? AND DEVICEID = ? AND VERSION = ?";
			break;
		case "5":
			updateQuery = "UPDATE USER_USAGE_LOG SET OPTIONS = OPTIONS + 1, LAST_MODIFIED = ? WHERE USER_ID = ? AND ACCESS_DATE = ?"
					+ "AND ENVIRONMENT = ? AND DEVICE = ? AND DEVICEID = ? AND VERSION = ?";
			break;
		case "6":
			updateQuery = "UPDATE USER_USAGE_LOG SET SYNC = SYNC + 1, LAST_MODIFIED = ? WHERE USER_ID = ? AND ACCESS_DATE = ?"
					+ "AND ENVIRONMENT = ? AND DEVICE = ? AND DEVICEID = ? AND VERSION = ?";
			break;
		case "7":
			updateQuery = "UPDATE USER_USAGE_LOG SET LOOKUP = LOOKUP + 1, LAST_MODIFIED = ? WHERE USER_ID = ? AND ACCESS_DATE = ?"
					+ "AND ENVIRONMENT = ? AND DEVICE = ? AND DEVICEID = ? AND VERSION = ?";
			break;
		default:
			break;
		}
	}

	private void fetchSqlInsertQuery(String type) {
		switch (type) {
		case "1":
			insertQuery = "INSERT INTO USER_USAGE_LOG (USER_ID,ACCESS_DATE,LOGIN,LOGOUT,ENTITLEMENT,SNAP,OPTIONS,SYNC,LAST_MODIFIED,LOOKUP,ENVIRONMENT,DEVICE,DEVICEID,VERSION) VALUES (?,?,1,0,0,0,0,0,?,0,?,?,?,?)";
			break;
		case "2":
			insertQuery = "INSERT INTO USER_USAGE_LOG (USER_ID,ACCESS_DATE,LOGIN,LOGOUT,ENTITLEMENT,SNAP,OPTIONS,SYNC,LAST_MODIFIED,LOOKUP,ENVIRONMENT,DEVICE,DEVICEID,VERSION) VALUES (?,?,0,1,0,0,0,0,?,0,?,?,?,?)";
			break;
		case "3":
			insertQuery = "INSERT INTO USER_USAGE_LOG (USER_ID,ACCESS_DATE,LOGIN,LOGOUT,ENTITLEMENT,SNAP,OPTIONS,SYNC,LAST_MODIFIED,LOOKUP,ENVIRONMENT,DEVICE,DEVICEID,VERSION) VALUES (?,?,0,0,1,0,0,0,?,0,?,?,?,?)";
			break;
		case "4":
			insertQuery = "INSERT INTO USER_USAGE_LOG (USER_ID,ACCESS_DATE,LOGIN,LOGOUT,ENTITLEMENT,SNAP,OPTIONS,SYNC,LAST_MODIFIED,LOOKUP,ENVIRONMENT,DEVICE,DEVICEID,VERSION) VALUES (?,?,0,0,0,1,0,0,?,0,?,?,?,?)";
			break;
		case "5":
			insertQuery = "INSERT INTO USER_USAGE_LOG (USER_ID,ACCESS_DATE,LOGIN,LOGOUT,ENTITLEMENT,SNAP,OPTIONS,SYNC,LAST_MODIFIED,LOOKUP,ENVIRONMENT,DEVICE,DEVICEID,VERSION) VALUES (?,?,0,0,0,0,1,0,?,0,?,?,?,?)";
			break;
		case "6":
			insertQuery = "INSERT INTO USER_USAGE_LOG (USER_ID,ACCESS_DATE,LOGIN,LOGOUT,ENTITLEMENT,SNAP,OPTIONS,SYNC,LAST_MODIFIED,LOOKUP,ENVIRONMENT,DEVICE,DEVICEID,VERSION) VALUES (?,?,0,0,0,0,0,1,?,0,?,?,?,?)";
			break;
		case "7":
			insertQuery = "INSERT INTO USER_USAGE_LOG (USER_ID,ACCESS_DATE,LOGIN,LOGOUT,ENTITLEMENT,SNAP,OPTIONS,SYNC,LAST_MODIFIED,LOOKUP,ENVIRONMENT,DEVICE,DEVICEID,VERSION) VALUES (?,?,0,0,0,0,0,0,?,1,?,?,?,?)";
			break;
		default:
			break;
		}
	}

}
