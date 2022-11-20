// Decompiled by DJ v3.11.11.95 Copyright 2009 Atanas Neshkov  Date: 7/11/2009 4:27:21 PM
// Home Page: http://members.fortunecity.com/neshkov/dj.html  http://www.neshkov.com/dj.html - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   TWriter.java

package net.paxcel.utils.compression;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TWriter extends OutputStream
{

    public TWriter(SocketChannel socketchannel)
        throws IOException
    {
        _channel = null;
        _channel = socketchannel;
    }

    public void write(int i)
        throws IOException
    {
        byte abyte0[] = new byte[1];
        abyte0[0] = (byte)i;
        ByteBuffer bytebuffer = ByteBuffer.wrap(abyte0);
        _channel.write(bytebuffer);
    }

    public void write(byte abyte0[])
        throws IOException
    {
        ByteBuffer bytebuffer = ByteBuffer.wrap(abyte0);
        _channel.write(bytebuffer);
    }

    public void write(byte abyte0[], int i, int j)
        throws IOException
    {
        ByteBuffer bytebuffer = ByteBuffer.wrap(abyte0, i, j);
        _channel.write(bytebuffer);
    }

    SocketChannel _channel;
}
