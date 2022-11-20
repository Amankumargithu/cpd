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
import javax.servlet.ServletOutputStream;
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

public class QuoddPriceViewerJwtAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddPriceViewerJwtAction.class);
	private static final Gson gson = new Gson();
	static {
		try {
			Environment.init();
		} catch (Exception e) {
			log.error("Cannot initiate environment properties");
		}
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		try {
			response.setContentType("text/json");
			response.addHeader("Access-Control-Allow-Origin", "*");
			response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS, HEAD, PUT, POST");
			response.addHeader("Access-Control-Allow-Headers", "*");
			String userId = request.getParameter("user_id");
			String email = request.getParameter("email");
			String path = Environment.get("MARKIT_PRIVATE_KEY_PATH");
			log.info("In  execute dir is " + path);
			if (path == null) {
				log.info("MARKIT_PRIVATE_KEY_PATH not set in environment.properties");
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
			long nowMillis = System.currentTimeMillis();
			Date now = new Date(nowMillis);
			long expiryMilliSeconds = nowMillis + (24 * 3600 * 1000);
			Date expiryDate = new Date(expiryMilliSeconds);
			jwtToken = Jwts.builder().setIssuedAt(now).setClaims(claims).setExpiration(expiryDate)
					.signWith(SignatureAlgorithm.RS512, privateKey).compact();
			log.info("Markit JWT Token " + jwtToken + " username: " + userName + " userid: " + userId);
			HashMap<String, String> obj = new HashMap<>();
			obj.put("token", jwtToken);
			try (ServletOutputStream sos = response.getOutputStream();) {
				sos.write(gson.toJson(obj).getBytes());
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddPriceViewerJwtAction.execute() : encountered exception. ", e);
			}
		} catch (Exception e) {
			log.error("QuoddPriceViewerJwtAction Unable to  generate JWT token", e);
		}
		return null;
	}
}
