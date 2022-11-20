package com.b4utrade.web.action;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
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

import com.google.gson.Gson;
import com.tacpoint.util.Environment;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class QuoddBlackBoxJwtAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddBlackBoxJwtAction.class);
	private static final Gson gson = new Gson();

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		try {
			response.setContentType("text/gson");
			response.addHeader("Access-Control-Allow-Origin", "*");
			response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS, HEAD, PUT, POST");
			response.addHeader("Access-Control-Allow-Headers", "*");
			String userId = request.getParameter("user_id");
			String email = request.getParameter("email");
			String firstName = request.getParameter("first_name");
			String lastName = request.getParameter("last_name");
			String userName = request.getParameter("user_name");
			String isOtc = request.getParameter("otc");
			Environment.init();
			String path = Environment.get("PRIVATE_KEY_PATH");
			log.info("In  execute dir is " + path);
			if (path == null) {
				log.info("PRIVATE_KEY_PATH not set in environment.properties");
				path = "./resources/private_key.der";
			}
			byte[] keyBytes = Files.readAllBytes(Paths.get(path));
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PrivateKey privateKey = kf.generatePrivate(spec);
			HashMap<String, Object> claims = new HashMap<>();
			String jwtToken = null;
			claims.put("user_id", userId);
			claims.put("email", email);
			claims.put("first_name", firstName);
			claims.put("last_name", lastName);
			claims.put("user_name", userName);
			claims.put("isOtc", isOtc);
			long nowMillis = System.currentTimeMillis();
			Date now = new Date(nowMillis);
			long expiryMilliSeconds = nowMillis + (10 * 24 * 3600 * 1000);
			Date expiryDate = new Date(expiryMilliSeconds);
			jwtToken = Jwts.builder().setIssuedAt(now).setClaims(claims).setExpiration(expiryDate)
					.signWith(SignatureAlgorithm.RS512, privateKey).compact();
			log.info("Karma JWT Token " + jwtToken + " username: " + userName + " userid: " + userId);
			HashMap<String, String> obj = new HashMap<>();
			obj.put("token", jwtToken);
			response.getOutputStream().write(gson.toJson(obj).getBytes());
		} catch (Exception e) {
			String msg = "QuoddBlackBoxJwtAction Unable to JWT token.";
			log.error(msg, e);
		}
		return null;
	}
}
