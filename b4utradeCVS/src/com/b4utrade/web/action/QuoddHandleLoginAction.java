package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bean.UserLoginStatusBean;
import com.b4utrade.bo.UserBO;
import com.b4utrade.cache.RTDUserSessionManager;
import com.b4utrade.helper.ConsoleTimestampLogger;
import com.b4utrade.helper.PasswordEncrypter;
import com.b4utrade.helper.SingleLoginHelper;
import com.tacpoint.util.Environment;

public class QuoddHandleLoginAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddHandleLoginAction.class);
	public static final String ACTION_CHECK_MOBIE_SESSION = "MOBILE_SESSION_EXIST";
	private HashMap<String, String> encryptedPassMap = new HashMap<String, String>();
	private HashMap<String, String> unEncryptedPassMap = new HashMap<String, String>();

	private FileWriter encWriter = null;
	private FileWriter unEncWriter = null;
	private static final String SOURCE = "ENVIRONMENT";
	private String passwordFromUI = "";
	private SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");

	public QuoddHandleLoginAction() {
		try {
			encWriter = new FileWriter("/usr/user_logs/encrypted_users.log");
			unEncWriter = new FileWriter("/usr/user_logs/unencrypted_users.log");
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						encWriter.close();
						unEncWriter.close();
						ConsoleTimestampLogger.println("files closed in shutdown hook");
					} catch (Exception e) {
						ConsoleTimestampLogger.println("problem while closing files");
					}
				}
			});
		} catch (IOException e) {
			ConsoleTimestampLogger.println(this.getClass().getName() + "problem: " + e.getMessage());
		}
	}

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
	 * @param response   The HTTP response we are creating
	 * 
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
		UserLoginStatusBean ulsb = null;

		try {
			long beginTime = System.currentTimeMillis();
			response.setContentType("text/html");
			String action = request.getParameter(UserLoginStatusBean.ACTION_KEY);
			ConsoleTimestampLogger.println("QuoddHandleLoginAction.execute() - action = " + action);
			if (action.equals(UserLoginStatusBean.ACTION_EXPIRE_SESSION)) {
				String userId = (String) request.getParameter(UserLoginStatusBean.USER_ID_KEY);
				ulsb = handleExpireSession(userId);
			} else if (action.equals(UserLoginStatusBean.ACTION_CHECK_SESSION)) {
				ulsb = handleCheckUserSession(request);
			} else if (action.equals(ACTION_CHECK_MOBIE_SESSION)) {
				ulsb = handleCheckMobileUserSession(request);
			} else {
				System.out.println(new Timestamp(System.currentTimeMillis()) + " Login Action Stat: Env - "
						+ request.getParameter("SYSTEM_NAME") + " Quodd Version - " + request.getParameter("VERSION")
						+ " JDK version - " + request.getParameter("JAVA_VERSION") + " UserID: "
						+ request.getParameter(UserLoginStatusBean.USERNAME_KEY) + " host Name: "
						+ request.getParameter("HOST_NAME") + " " + request.getRequestURI() + " "
						+ request.getRequestURL());
				ulsb = handleLogin(request, response);
				String currentEQVersion = Environment.get("CURRENT_EQ_VERSION");
				String userVersion = request.getParameter("VERSION");
				if (currentEQVersion != null && userVersion != null) {
					try {
						currentEQVersion = currentEQVersion.trim();
						String currentArray[] = currentEQVersion.split("\\.");
						String userArray[] = userVersion.split("\\.");
						if (currentArray.length == userArray.length) {
							for (int i = 0; i < userArray.length; i++) {
								if (Integer.parseInt(currentArray[i]) < Integer.parseInt(userArray[i])) {
									break;
								}
								if (Integer.parseInt(currentArray[i]) > Integer.parseInt(userArray[i])) {
									String expiryFlag = Environment.get("EQ_EXPIRY_FLAG");
									System.out.println("Expiry flag is : " + expiryFlag);
									if (expiryFlag != null && expiryFlag.trim().equalsIgnoreCase("TRUE")) {
										String expiryDate = Environment.get("EQ_EXPIRY_DATE");
										System.out.println("Expiry date is : " + expiryDate);
										expiryDate = expiryDate.trim();
										if (expiryDate != null) {
											long days = calculateDays(expiryDate);
											if (days == 0) {
												ulsb.setStatusLoginFailed();
												ulsb.setMessage(
														"This version has expired. Please update.\n\n Need Help? \n Please contact QUODD Support at 866-537-5518 or support@quodd.com.");
											} else {
												ulsb.setMessage("This version of QUODD Equity+ will expire in " + days
														+ " days.\n Please update to new version to continue to use QUODD Equity+.\n\n Need Help? \n Please contact QUODD Support at 866-537-5518 "
														+ "or support@quodd.com.");
											}
										}
									} else {
										ulsb.setMessage(
												"New version of QUODD Equity+ is now available.\n\n Need Help? \n Please contact QUODD Support at 866-537-5518 or support@quodd.com.");
									}
									break;
								}
							}
						}
					} catch (Exception e) {
						System.out.println(
								"Exception while comparing login versions in QuoddHandleLoginAction current version : "
										+ currentEQVersion + " userversion : " + request.getParameter("VERSION")
										+ " userID: " + request.getParameter(UserLoginStatusBean.USERNAME_KEY));
						e.printStackTrace();
					}
				}
			}
			ConsoleTimestampLogger.println("Login request source is: " + request.getParameter(SOURCE));
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			XMLEncoder encoder = new XMLEncoder(byteArray);
			encoder.writeObject(ulsb);
			encoder.close();

			ServletOutputStream sos = null;
			try {
				sos = response.getOutputStream();
				sos.write(byteArray.toByteArray());
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddHandleLoginAction encountered exception. ", e);
			} finally {
				if (sos != null) {
					try {
						sos.close();
					} catch (Exception e) {
					}
				}
			}
			long endTime = System.currentTimeMillis();
			ConsoleTimestampLogger.println("Total time to process login : " + (endTime - beginTime));
			ConsoleTimestampLogger.println("response attr " + response.toString());
		} catch (Exception e) {
			log.error("QuoddHandleLoginAction.execute - unable to locate user=" + userName);
			return null;
		}
		return (null);
	}

	private UserLoginStatusBean handleCheckMobileUserSession(HttpServletRequest request) {
		ConsoleTimestampLogger
				.println("QuoddHandleinAction.handleCheckMobileUserSession : Start Session Checking Process");
		UserLoginStatusBean ulsb = new UserLoginStatusBean();
		int userID = 0;
		try {
			HttpSession hs = request.getSession(false);
			if (hs == null) {
				ConsoleTimestampLogger.println("QuoddHandleLoginAction.handleCheckMobileUserSession : Session is null");
				ulsb.setStatusLoginDuplicate();
				return ulsb;
			}
			ConsoleTimestampLogger.println("QuoddHandleLoginAction.handleCheckMobileUserSession : Session is not null");
			String userIDfromServerSession = (String) hs.getValue("USERID");
			if (userIDfromServerSession == null) {
				ConsoleTimestampLogger.println(
						"QuoddHandleLoginAction.handleCheckMobileUserSession : UserID is null in HttpSession Obj");
				ConsoleTimestampLogger.println(
						"QuoddHandleLoginAction.handleCheckMobileUserSession : Checking UserID in Query Params");
				String userIdInParam = (String) request.getParameter(UserLoginStatusBean.USER_ID_KEY);
				if (userIdInParam == null || userIdInParam.isEmpty()) {
					ConsoleTimestampLogger.println(
							"QuoddHandleLoginAction.handleCheckMobileUserSession : UserID is null in Query Params");
					ulsb.setStatusLoginDuplicate();
					return ulsb;
				}
				try {
					userID = Integer.parseInt(userIdInParam);
				} catch (Exception e) {
					log.error("QuoddHandleLoginAction.handleCheckMobileUserSession - Unable to parse user ID");
					ulsb.setStatusLoginDuplicate();
					return ulsb;
				}
				ConsoleTimestampLogger.println(
						"QuoddHandleLoginAction.handleCheckMobileUserSession : Fetched UserID from Query Params :="
								+ userID);
			} else {
				ConsoleTimestampLogger.println(
						"QuoddHandleLoginAction.handleCheckMobileUserSession - User id : " + userIDfromServerSession);
				try {
					userID = Integer.parseInt(userIDfromServerSession);
				} catch (Exception e) {
					log.error("QuoddHandleLoginAction.handleCheckMobileUserSession - Unable to parse user ID");
					ulsb.setStatusLoginDuplicate();
					return ulsb;
				}
			}
			String sessionID = (String) hs.getId();
			ConsoleTimestampLogger
					.println("QuoddHandleLoginAction.handleCheckMobileUserSession - User session id : " + sessionID);
			SingleLoginHelper slh = new SingleLoginHelper(userID);
			if (slh.isCurrentUserLoggedOn()) {
				String currentSession = slh.getSessionID();
				ConsoleTimestampLogger
						.println("QuoddHandleLoginAction.handleCheckMobileUserSession : User is currently login");
				ConsoleTimestampLogger.println("Current session from request is: " + sessionID);
				ConsoleTimestampLogger.println("Current session in DB is: " + currentSession);
				ulsb.setUserID(userID);
				if (currentSession != null && currentSession.equalsIgnoreCase(sessionID)) {
					ConsoleTimestampLogger
							.println("QuoddHandleLoginAction.handleCheckMobileUserSession : User session is the same");
					ulsb.setStatusLoginSuccess();
				} else {
					ConsoleTimestampLogger.println(
							"QuoddHandleLoginAction.handleCheckMobileUserSession : User session is not the same");
					ulsb.setStatusLoginDuplicate();
				}
			} else {
				ulsb.setUserID(Integer.valueOf(userID));
				ulsb.setStatusLoginDuplicate();
			}
			ConsoleTimestampLogger
					.println("QuoddHandleLoginAction.handleCheckMobileUserSession : End Session Checking Process");
			return ulsb;
		} catch (Exception e) {
			ulsb.setUserID(Integer.valueOf(userID));
			ulsb.setStatusLoginDuplicate();
			return ulsb;
		}
	}

	private UserLoginStatusBean handleLogin(HttpServletRequest request, HttpServletResponse response) {
		UserLoginStatusBean ulsb = new UserLoginStatusBean();
		if (request.getParameter(UserLoginStatusBean.USERNAME_KEY) != null)
			userName = (String) request.getParameter(UserLoginStatusBean.USERNAME_KEY);
		if (request.getParameter(UserLoginStatusBean.PASSWORD_KEY) != null)
			passwordFromUI = (String) request.getParameter(UserLoginStatusBean.PASSWORD_KEY);
		log.info("username is : " + userName + "  password is : " + passwordFromUI);
		if (userName == null || userName.isEmpty() || passwordFromUI == null || passwordFromUI.isEmpty()) {
			log.warn("QuoddHandleLogin .handleLogin username or password is null in queryParam");
			ulsb.setStatusLoginFailed();
			return ulsb;
		}
		try {
			UserBO uBo = new UserBO();
			uBo.setUserName(userName);
			uBo.setUserPassword(passwordFromUI);
			String token = uBo.handleLogin(ulsb);
			if (ulsb.isLoginFailed()) {
				return ulsb;
			} else {
				int userId = ulsb.getUserID(); // qss4Id
				log.info("QuoddHandleLoginAction.handleLogin(): token retrieved from qss4 auth api :" + token
						+ " for userid : " + userId + " username " + userName);
				try {
					if (passwordFromUI.startsWith(PasswordEncrypter.ENCRYPT_PREFIX)) {
						if (!encryptedPassMap.containsKey(userName)) {
							encWriter.write(userName + "\n");
							encWriter.flush();
						}
						encryptedPassMap.put(userName, "");
					} else {
						if (!unEncryptedPassMap.containsKey(userName)) {
							unEncWriter.write(userName + "\n");
							unEncWriter.flush();
						}
						unEncryptedPassMap.put(userName, "");
					}
				} catch (Exception e) {
					ConsoleTimestampLogger.println(this.getClass().getName() + " problem while writing logs to file");
				}
				ConsoleTimestampLogger.println("QuoddHandleLoginAction.handleLogin - in Cache User id = " + userId);
				try {
					HttpSession hs = request.getSession(false);
					Enumeration<String> attributes = hs.getAttributeNames();
					while (attributes.hasMoreElements()) {
						String attribute = attributes.nextElement();
						hs.removeAttribute(attribute);
					}
					hs.invalidate();
				} catch (Exception e) {
				}
				// generating new session
				HttpSession hs = request.getSession(true);
				ConsoleTimestampLogger.println(
						"Got session ID: " + hs.getId() + " for user: " + userName + " and userID : " + userId);
				SingleLoginHelper slh = new SingleLoginHelper();
				slh.setUserID(Integer.valueOf(userId));
				slh.setSessionID(hs.getId());
				slh.setSource(request.getParameter(SOURCE));
				// adding user session in mysql db
				slh.addLoggedInUser();
				if (slh.isCurrentUserLoggedOn()) {
					ConsoleTimestampLogger
							.println("QuoddHandleLoginAction.handleLogin - User : " + userId + " already login");
					String source = slh.getSource();
					if (source != null && source.equals("RTD")) {
						ConsoleTimestampLogger.println("source is RTD :  : " + source + "  userid is " + userId);
						RTDUserSessionManager.getInstance().deleteUserSession(userId);
					} else {
						ConsoleTimestampLogger.println("source is : " + source);
					}
					UserLoginStatusBean expireUserObj = handleExpireSession(String.valueOf(userId));
					if (expireUserObj.isExpireOtherFailed()) {
						ConsoleTimestampLogger.println("Unable to expire the user: " + userName);
						ulsb.setStatusLoginDuplicate();
						return ulsb;
					}
				}
				// NEED THESE VALUES, SO THAT WE CAN RETAIN ORIGINAL SESSION ID
				slh.setUserID(Integer.valueOf(userId));
				slh.setSessionID(hs.getId());
				slh.setSource(request.getParameter(SOURCE));
				hs.putValue("SLH", slh);
				hs.putValue("USERID", "" + userId);
				hs.putValue("USERNAME", userName);
				hs.putValue("PASSWORD", passwordFromUI);
				hs.putValue("USERFIRSTNAME", ulsb.getFirstName());
				hs.putValue("QUODD_JWT", token);
				hs.setAttribute("SLH", slh);
				hs.setAttribute("USERID", "" + userId);
				hs.setAttribute("USERNAME", "" + userName);
				hs.setAttribute("PASSWORD", "" + passwordFromUI);
				hs.setAttribute("USERFIRSTNAME", "" + ulsb.getFirstName());
				hs.setAttribute("QUODD_JWT", token);
				ConsoleTimestampLogger.println(
						new Timestamp(System.currentTimeMillis()) + " user id is : " + userId + "  session is : " + hs);
				ulsb.setStatusLoginSuccess();
				try {
					Cookie tokenCookie = new Cookie("QUODD_JWT", token);
					response.addCookie(tokenCookie);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			ulsb.setStatusLoginFailed();
			e.printStackTrace();
		}
		return ulsb;
	}

	private UserLoginStatusBean handleExpireSession(String userId) {
		ConsoleTimestampLogger.println("QuoddHandleLoginAction.handleExpireSession : Start Session Expire process");
		UserLoginStatusBean ulsb = new UserLoginStatusBean();

		try {
			ConsoleTimestampLogger.println("About to expire user: " + userId);
			if ((userId != null) && (userId.length() > 0)) {
				SingleLoginHelper sh = new SingleLoginHelper(Integer.parseInt(userId));
				sh.deleteUser();
				ulsb.setUserID(Integer.valueOf(userId));
				ulsb.setStatusExpireOtherSuccess();
			} else {
				ulsb.setUserID(Integer.valueOf(userId));
				ulsb.setStatusExpireOtherFail();
				ulsb.setMessage("User ID is null or 0");
			}
			ConsoleTimestampLogger.println("QuoddHandleLoginAction.handleExpireSession : End Session Expire process");
			return ulsb;
		} catch (Exception e) {
			ulsb.setUserID(Integer.valueOf(userId));
			ulsb.setStatusExpireOtherFail();
			return ulsb;
		}
	}

	private UserLoginStatusBean handleCheckUserSession(HttpServletRequest request) {
		ConsoleTimestampLogger.println("QuoddHandleinAction.handleCheckUserSession : Start Session Checking Process");
		UserLoginStatusBean ulsb = new UserLoginStatusBean();
		try {
			HttpSession hs = request.getSession(false);
			if (hs == null) {
				ConsoleTimestampLogger.println("QuoddHandleLoginAction.handleCheckUserSession : Session is null");
				ulsb.setStatusLoginDuplicate();
				return ulsb;
			}
			ConsoleTimestampLogger.println("QuoddHandleLoginAction.handleCheckUserSession : Session is not null");
			String userIDfromServerSession = (String) hs.getValue("USERID");
			if (userIDfromServerSession == null) {
				ConsoleTimestampLogger
						.println("QuoddHandleLoginAction.handleCheckUserSession : UserID is null in HttpSession Obj");
				ulsb.setStatusLoginFailed();
				return ulsb;
			}
			ConsoleTimestampLogger
					.println("QuoddHandleLoginAction.handleCheckUserSession - User id : " + userIDfromServerSession);
			int userID = 0;
			try {
				userID = Integer.parseInt(userIDfromServerSession);
			} catch (Exception e) {
				log.error("QuoddHandleLoginAction.handleCheckUserSession - Unable to parse user ID");
				ulsb.setStatusLoginFailed();
				return ulsb;
			}
			String sessionID = (String) hs.getId();
			ConsoleTimestampLogger
					.println("QuoddHandleLoginAction.handleCheckUserSession - User session id : " + sessionID);
			SingleLoginHelper slh = new SingleLoginHelper(userID);
			if (slh.isCurrentUserLoggedOn()) {
				String currentSession = slh.getSessionID();
				ConsoleTimestampLogger
						.println("QuoddHandleLoginAction.handleCheckUserSession : User is currently login");
				ConsoleTimestampLogger.println("Current session from request is: " + sessionID);
				ConsoleTimestampLogger.println("Current session in DB is: " + currentSession);
				ulsb.setUserID(userID);
				if (currentSession != null && currentSession.equalsIgnoreCase(sessionID)) {
					ConsoleTimestampLogger
							.println("QuoddHandleLoginAction.handleCheckUserSession : User session is the same");
					ulsb.setStatusLoginSuccess();
				} else {
					ConsoleTimestampLogger
							.println("QuoddHandleLoginAction.handleCheckUserSession : User session is not the same");
					ulsb.setStatusLoginDuplicate();
				}
			} else {
				ConsoleTimestampLogger
						.println("QuoddHandleLoginAction.handleCheckUserSession : User is not currently login");
				try {
					slh.addLoggedInUser();
					ConsoleTimestampLogger.println(
							" now adding SingleLoginHelper: valueBound() - User  successfuly saved to the current users table.");
					ulsb.setStatusLoginSuccess();
				} catch (Exception be) {
					be.printStackTrace();
				}
			}
			ConsoleTimestampLogger
					.println("QuoddHandleLoginAction.handleCheckUserSession : End Session Checking Process");
			return ulsb;
		} catch (Exception e) {
			ulsb.setStatusSessionCheckFailed();
			return ulsb;
		}
	}

	private long calculateDays(String endDate) {
		Date sDate = new Date();
		Date eDate = null;
		try {
			eDate = format.parse(endDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Calendar cal3 = Calendar.getInstance();
		cal3.setTime(sDate);
		Calendar cal4 = Calendar.getInstance();
		cal4.setTime(eDate);
		return daysBetween(cal3, cal4);
	}

	private long daysBetween(Calendar startDate, Calendar endDate) {
		Calendar date = (Calendar) startDate.clone();
		long daysBetween = 0;
		while (date.before(endDate)) {
			date.add(Calendar.DAY_OF_MONTH, 1);
			daysBetween++;
		}
		return daysBetween;
	}

}
