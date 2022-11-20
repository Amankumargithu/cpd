// Decompiled by DJ v3.11.11.95 Copyright 2009 Atanas Neshkov  Date: 7/11/2009 4:26:15 PM
// Home Page: http://members.fortunecity.com/neshkov/dj.html  http://www.neshkov.com/dj.html - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   Element.java

package net.paxcel.utils.compression;


class Element
{

    public Element(int i, int j)
    {
        prefix = 0;
        suffix = 0;
        prefix = i;
        suffix = j;
    }

    int prefix;
    int suffix;
}
