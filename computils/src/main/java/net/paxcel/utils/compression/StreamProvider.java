// Decompiled by DJ v3.11.11.95 Copyright 2009 Atanas Neshkov  Date: 7/11/2009 4:27:10 PM
// Home Page: http://members.fortunecity.com/neshkov/dj.html  http://www.neshkov.com/dj.html - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   StreamProvider.java

package net.paxcel.utils.compression;

import java.io.*;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;

// Referenced classes of package net.paxcel.utils.compression:
//            TWriter, LZWOutputStream, LZWInputStream

public class StreamProvider
{

    public StreamProvider()
    {
    }

    public static OutputStream getOutputStream(SocketChannel socketchannel)
        throws IOException
    {
        if(StreamsTable.containsKey(new Integer(socketchannel.hashCode())))
        {
            return (OutputStream)StreamsTable.get(new Integer(socketchannel.hashCode()));
        } else
        {
            TWriter twriter = new TWriter(socketchannel);
            BufferedOutputStream bufferedoutputstream = new BufferedOutputStream(twriter, 128);
            LZWOutputStream lzwoutputstream = new LZWOutputStream(bufferedoutputstream);
            StreamsTable.put(new Integer(socketchannel.hashCode()), lzwoutputstream);
            return lzwoutputstream;
        }
    }

    public static InputStream getInputStream(InputStream inputstream)
        throws IOException
    {
        return new LZWInputStream(inputstream, false);
    }

    private static Hashtable StreamsTable = new Hashtable();

}
