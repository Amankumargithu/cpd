// Decompiled by DJ v3.11.11.95 Copyright 2009 Atanas Neshkov  Date: 7/11/2009 4:26:59 PM
// Home Page: http://members.fortunecity.com/neshkov/dj.html  http://www.neshkov.com/dj.html - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   StatisticalAnalyzer.java

package net.paxcel.utils.compression;

import java.io.IOException;
import java.util.Calendar;
import java.util.logging.*;

public class StatisticalAnalyzer
{

    public StatisticalAnalyzer()
    {
    }

    public static void addReportPoint(int i, int j, int k)
    {
        reportPoints[numPoints++] = i * 3600 + j * 60 + k;
    }

    public static void addRawCount(long l)
    {
        if(!started)
            Start();
        rawCount += l;
        DoStat();
    }

    public static void addCompressedCount(long l)
    {
        if(!started)
            Start();
        compressedCount += l;
        DoStat();
    }

    private static void DoStat()
    {
        if(isReportTime())
        {
            generateReport();
            nextPoint++;
        }
    }

    private static boolean isReportTime()
    {
        if(nextPoint >= numPoints)
            return false;
        return (Calendar.getInstance().getTimeInMillis() - startTime) / 1000L >= reportPoints[nextPoint];
    }

    private static void generateReport()
    {
        String s = new String();
        long l = reportPoints[nextPoint];
        s = l / 3600L + ":";
        l %= 3600L;
        s = s + l / 60L + ":";
        l %= 60L;
        s = s + l;
        logString(s, String.valueOf(rawCount), String.valueOf(compressedCount), String.valueOf((((double)rawCount - (double)compressedCount) / (double)rawCount) * 100D) + "%", "");
    }

    private static void Start()
    {
        FileHandler filehandler = null;
        try
        {
            filehandler = new FileHandler("Comp.stats");
        }
        catch(SecurityException securityexception)
        {
            securityexception.printStackTrace();
        }
        catch(IOException ioexception)
        {
            ioexception.printStackTrace();
        }
        addReportPoint(0, 0, 5);
        addReportPoint(0, 0, 10);
        addReportPoint(0, 0, 15);
        addReportPoint(0, 0, 20);
        addReportPoint(0, 1, 0);
        addReportPoint(0, 2, 0);
        addReportPoint(0, 15, 0);
        statLogger.addHandler(filehandler);
        statLogger.isLoggable(Level.ALL);
        startTime = Calendar.getInstance().getTimeInMillis();
        logString("Time", "   Raw", "  Comp   ", "Percent", "");
        started = true;
    }

    private static void logString(String s, String s1, String s2, String s3, String s4)
    {
        statLogger.log(Level.INFO, "   " + s + "    " + s1 + "    " + s2 + "    " + s3 + "    " + s4);
    }

    static final int MAX_POINTS = 10;
    static long reportPoints[] = new long[10];
    static long startTime = 0L;
    static int numPoints = 0;
    static int nextPoint = 0;
    static long rawCount = 0L;
    static long compressedCount = 0L;
    static boolean started = false;
    static Logger statLogger = Logger.getLogger("Stats");

}
