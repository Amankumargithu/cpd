// Decompiled by DJ v3.11.11.95 Copyright 2009 Atanas Neshkov  Date: 7/11/2009 4:26:46 PM
// Home Page: http://members.fortunecity.com/neshkov/dj.html  http://www.neshkov.com/dj.html - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   LZWOutputStream.java

package net.paxcel.utils.compression;

import java.io.*;
import java.util.Hashtable;

public class LZWOutputStream extends FilterOutputStream
{

    public LZWOutputStream(OutputStream outputstream)
    {
        super(outputstream);
        table = null;
        codeUsed = 256;
        pcode = -1;
        table = new Hashtable();
        for(int i = 0; i < 256; i++)
            table.put(new Integer(i), new Integer(i));

    }

    private void output(int i)
        throws IOException
    {
        if(bitsLeftOver)
        {
            int l = i & 0xff;
            int j = (leftOver << 4) + (i >> 8);
            out.write(j);
            out.write(l);
            bitsLeftOver = false;
        } else
        {
            leftOver = i & 0xf;
            int k = i >> 4;
            out.write(k);
            bitsLeftOver = true;
        }
    }

    private void flush0()
        throws IOException
    {
        output(pcode);
        if(bitsLeftOver)
            out.write(leftOver << 4);
    }

    private void compressByte(int i)
        throws IOException
    {
        if(pcode == -1)
        {
            pcode = i;
            return;
        }
        int j = (pcode << 8) + i;
        Integer integer = (Integer)table.get(new Integer(j));
        if(integer == null)
        {
            output(pcode);
            if(codeUsed < 4096)
                table.put(new Integer((pcode << 8) + i), new Integer(codeUsed++));
            pcode = i;
        } else
        {
            pcode = integer.intValue();
        }
    }

    public void close()
        throws IOException
    {
        flush0();
        out.close();
    }

    public void flush()
        throws IOException
    {
        flush0();
    }

    public void write(byte abyte0[], int i, int j)
        throws IOException
    {
        for(int k = i; k < i + j; k++)
            write(abyte0[k]);

    }

    public void write(byte abyte0[])
        throws IOException
    {
        for(int i = 0; i < abyte0.length; i++)
            write(abyte0[i]);

    }

    public void write(int i)
        throws IOException
    {
        compressByte(i);
    }

    static final int MAX_CODES = 4096;
    static final int BYTE_SIZE = 8;
    static final int EXCESS = 4;
    static final int ALPHA = 256;
    static final int MASK1 = 255;
    static final int MASK2 = 15;
    int leftOver;
    boolean bitsLeftOver;
    Hashtable table;
    int codeUsed;
    int pcode;

/*    public static void main (String args[])
    {
    
    	try
    	{
    		InputStream in = new FileInputStream("123_out.doc");
    		OutputStream out = new FileOutputStream ("dummOut.doc");
    		OutputStream output = new LZWOutputStream(out);
    		int read = -1;
    		while ((read = in.read())!=-1)
    		output.write(read);
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    }*/

}
