// Decompiled by DJ v3.11.11.95 Copyright 2009 Atanas Neshkov  Date: 7/11/2009 4:26:34 PM
// Home Page: http://members.fortunecity.com/neshkov/dj.html  http://www.neshkov.com/dj.html - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   LZWInputStream.java

package net.paxcel.utils.compression;

import java.io.*;

// Referenced classes of package net.paxcel.utils.compression:
//            Element, StatisticalAnalyzer

public class LZWInputStream extends FilterInputStream
{

    public LZWInputStream(InputStream inputstream, boolean flag)
        throws IOException
    {
        super(inputstream);
        codeUsed = 256;
        pcode = -1;
        start = true;
        end = false;
        outbuf = null;
        inbuf = null;
        _analyze = false;
        inbuf = new PipedInputStream();
        outbuf = new PipedOutputStream(inbuf);
        s = new int[4096];
        h = new Element[4096];
        _analyze = flag;
    }

    private void output(int i)
        throws IOException
    {
        size = -1;
        for(; i >= 256; i = h[i].prefix)
            s[++size] = h[i].suffix;

        s[++size] = i;
        for(int j = size; j >= 0; j--)
            outbuf.write(s[j]);

        if(_analyze)
            StatisticalAnalyzer.addRawCount((long)size + 1L);
    }

    private int getCode()
        throws IOException
    {
        int i = in.read();
        if(_analyze)
            StatisticalAnalyzer.addCompressedCount(1L);
        if(i == -1)
        {
            end = true;
            return -1;
        }
        int j = 0;
        if(bitsLeftOver)
        {
            j = (leftOver << 8) + i;
        } else
        {
            int k = in.read();
            if(_analyze)
                StatisticalAnalyzer.addCompressedCount(1L);
            j = (i << 4) + (k >> 4);
            leftOver = k & 0xf;
        }
        bitsLeftOver = !bitsLeftOver;
        return j;
    }

    private void decompress(int i)
        throws IOException
    {
        if(start)
        {
            pcode = getCode();
            s[0] = pcode;
            outbuf.write(s[0]);
            start = false;
        }
        if(pcode >= 0)
        {
            size = 0;
            do
            {
                int j = getCode();
                if(j < 0)
                    break;
                if(j < codeUsed)
                {
                    output(j);
                    if(codeUsed < 4096)
                        h[codeUsed++] = new Element(pcode, s[size]);
                } else
                {
                    h[codeUsed++] = new Element(pcode, s[size]);
                    output(j);
                }
                pcode = j;
            } while(inbuf.available() < i && !end);
        }
    }

    private int readEnd()
        throws IOException
    {
        if(inbuf.available() >= 1)
            return inbuf.read();
        else
            return -1;
    }

    public int read()
        throws IOException
    {
        if(end)
            return readEnd();
        if(inbuf.available() >= 1)
            return inbuf.read();
        decompress(1);
        if(!end && inbuf.available() >= 1)
            return inbuf.read();
        if(end)
            return readEnd();
        else
            return -1;
    }

    private int readEnd(byte abyte0[], int i, int j)
        throws IOException
    {
        if(inbuf.available() > 0)
            return inbuf.read(abyte0, i, j);
        else
            return -1;
    }

    public int read(byte abyte0[], int i, int j)
        throws IOException
    {
        int k = 0;
        if(end)
            return readEnd(abyte0, i, j);
        if(!end && inbuf.available() > 0)
            return inbuf.read(abyte0, i, j);
        k = j >= 128 ? 128 : j;
        decompress(k);
        if(!end && inbuf.available() > 0)
            return inbuf.read(abyte0, i, j);
        if(end)
            return readEnd(abyte0, i, j);
        else
            return -1;
    }

    public int read(byte abyte0[])
        throws IOException
    {
        return read(abyte0, 0, abyte0.length);
    }

    public void close()
        throws IOException
    {
        in.close();
    }

    static final int MAX_CODES = 4096;
    static final int BYTE_SIZE = 8;
    static final int EXCESS = 4;
    static final int ALPHA = 256;
    static final int MASK = 15;
    static final int BUFFER_SIZE = 128;
    int codeUsed;
    int s[];
    int size;
    Element h[];
    int leftOver;
    boolean bitsLeftOver;
    int pcode;
    boolean start;
    boolean end;
    PipedOutputStream outbuf;
    PipedInputStream inbuf;
    boolean _analyze;
    
    public static void main (String args[])
    {
    
    	try
    	{
    		InputStream in = new FileInputStream("123_2000_GOOG.txt");
    		InputStream input = new LZWInputStream(in,true);
    		OutputStream out = new FileOutputStream ("dummOut.doc");
    		int read = -1;
/*    		int count = 0;
    		int loop = 0;
    		while(loop < 2){
    			count = 0;
	    		while(count != 100){
	    			if(count == 98)
	    				System.out.println("98");
	    			read = input.read();
	    			out.write(read);
	    			count++;
	    		}
	    		loop++;
    		}
   */ 		
    		
    		while ((read = input.read())!=-1)
    		out.write(read);
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    }
}


