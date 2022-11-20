package abcd;
import java.io.*;
import java.net.*;

public class Httpsendrequest {

	public static void main(String[] args) {
	try {
		Httpsendrequest.MyGETRequest();
	} catch(Exception e) {
		e.printStackTrace();
	}

	}
	public static void MyGETRequest() throws IOException {
    URL url = new URL("https://gillcogroup.com/");
	    HttpURLConnection conection = (HttpURLConnection) url.openConnection();
	    conection.setRequestMethod("GET");
	    int responseCode = conection.getResponseCode();
	     if (responseCode == 400) {
	    	System.out.println("Bad request");
	       }  else if ( responseCode == 401) {
		    	System.out.println("unauthorized access");

	       } else if (responseCode == 403) {
		    	System.out.println("forbidden");

	       } else if (responseCode == 500) {
		    	System.out.println("internal server error");

	       }  else if (responseCode == 503) {
		    	System.out.println("service unavailable");

	       } else  {
		    	System.out.println("status ok");

	    	   
	       }
			String line = "";
			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(conection.getInputStream()));) {
				line = br.readLine();
				while(line != null) {
					System.out.println(line);
					line = br.readLine();
				

				}
				
		//	System.out.println(	line.contains("title"));
			}
			

	    
	}


}
	