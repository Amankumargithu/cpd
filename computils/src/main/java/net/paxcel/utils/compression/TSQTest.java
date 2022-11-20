package net.paxcel.utils.compression;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import net.paxcel.utils.compression.StreamProvider;


public class TSQTest {

	public TSQTest() throws Exception{
		Socket s = new Socket("216.150.131.92", 80);
		String path = "streaming/subscribe"; 
		String data ="USERID=2772_12&TOPICS=GOOG&SUBEND=true";
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), "UTF8")); 
		wr.write("GET "+path +" HTTP/1.0 \r\n"); wr.write("Content-Length: "+data.length() +"\r\n"); 
		System.out.println(path + data); 
		wr.write("\r\n"); 
		wr.write(data+" "); 
		wr.flush();
	 
		InputStream temp = s.getInputStream();		
		byte headers [] = new byte [83];
		int length = temp.read(headers,0,83);
		System.out.println("Length : " + length);
		System.out.println("Header Response is: " + new String(headers));

		/*OutputStream out = new FileOutputStream ("123.txt");
		int read1;
		int c = 0;
		while(c<500){
			read1 = temp.read();
			out.write(read1);
			c++;
		}*/
	    
		InputStream bis = StreamProvider.getInputStream(s.getInputStream());
		byte b [] = new byte [1024];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (true)
		{
			int read = bis.read(b);
			byte[] bytes = null;
			for (int i=0; i<read; i++) {
				if (b[i] ==  63 ) // line separator 
				{
					bytes = baos.toByteArray();
					if (bytes.length > 0) {
						System.out.println("Data received from streamer --> " + new String(bytes));
					}
					baos.reset();
				}
				else {
					baos.write(b,i,1);
				}
			}
		 }         
         
/*		URL url = new URL ("http://216.150.131.73/streaming/subscribe");
		URLConnection conn = url.openConnection();
		String data ="?USERID=2772_12&TOPICS=GOOG,C,IBM&SUBEND=true";
		 conn.setDoOutput(true);
         conn.setDoInput(true);
         conn.setAllowUserInteraction(false);
         conn.setUseCaches(false);
         OutputStream os = conn.getOutputStream();
         os.write(data.toString().getBytes());
         InputStream bis = StreamProvider.getInputStream(conn.getInputStream());//StreamProvider.getInputStream(s.getInputStream());
         byte b [] = new byte [1024];
   		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (true)
        {
        	try{
         		int read = bis.read(b);
   
                    for (int i=0; i<read; i++) 
                    {


                        if (b[i] == 63) {
                     	      if (baos.toByteArray().length > 0) 
                     	      {
                                             	   System.out.println(parseKeyValues(baos.toByteArray()));
                     	      }
                     	   baos.reset();
                        }
                        else 
                     	   baos.write(b,i,1);
                    }
                 
         			}catch (Exception e)
         			{
         				e.printStackTrace();
         			}
         			
         		}
*/	}

	 public static Map parseKeyValues(byte[] bytes) {

	      Hashtable map = new Hashtable();

	      StringTokenizer st = new StringTokenizer(new String(bytes),"|");

	      try {
	         while (st.hasMoreTokens()) {
	            String tuple = st.nextToken();
	            int index = tuple.indexOf(":");

	            //System.out.println("TSQMessageSerializer key : "+tuple.substring(0,index) + " value : "+tuple.substring(index+1));

	            map.put(tuple.substring(0,index),tuple.substring(index+1));
	         }
	      }
	      catch (Exception ex) {
		 System.out.println("TSQMessageSerializer - Error parsing key values - "+ex.getMessage()+ " "+new String(bytes));
	      }

	      return map;
	   }
	
	public static void main (String []args) throws Exception
	{
		new TSQTest ();
	}


}
