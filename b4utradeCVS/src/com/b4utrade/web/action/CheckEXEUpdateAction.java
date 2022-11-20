package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.tacpoint.util.Environment;

public class CheckEXEUpdateAction extends B4UTradeDefaultAction{

	static Log log = LogFactory.getLog(CheckEXEUpdateAction.class);
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {

		try {

			String version = request.getParameter("VERSION");
			log.info("Version in CheckEXEUpdateAction is : "+version);
			String url = Environment.get("EXE_VERSION_CHECK_URL");
			if(url == null){
				url ="http://eqp.quodd.com/downloads/quodd_exe_";
			}
			String fileURL = url+version+".txt";
			log.info("fileURL in CheckEXEUpdateAction is : "+fileURL);
			File f = new File(fileURL);
			if(!f.exists())
				return null;
			URL file = new URL(fileURL);
			BufferedReader in = new BufferedReader(new InputStreamReader(file.openStream()));
			String inputLine = in.readLine();
			in.close();			

			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			XMLEncoder encoder = new XMLEncoder(byteArray);
			encoder.writeObject(inputLine);
			encoder.close();
			log.info("input line in CheckEXEUpdateAction is : "+inputLine);
			ServletOutputStream sos = null;
			try
			{
				sos = response.getOutputStream();
				sos.write(byteArray.toByteArray());
				sos.flush();
			} 
			catch (Exception e)
			{
				log.error("CheckEXEUpdateAction.execute() : servlet output stream encountered exception. ", e);
			}
			finally
			{
				if (sos != null)
				{
					try
					{
						sos.close();
					} 
					catch (Exception e)
					{}
				}
			}       
		} catch (Exception e) {
			log.error("CheckEXEUpdateAction.execute() : exception. ",e);
		}

		return null;
	}
}
