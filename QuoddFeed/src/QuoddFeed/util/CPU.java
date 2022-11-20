/******************************************************************************
*
*  CPU.java
*     Wrapper around libcpu.so
*
*  REVISION HISTORY:
*     13 OCT 2011 jcs  Created.
*     28 JUN 2012 jcs  java.lang.management
*
*  (c) 1994-2012 Gatea Ltd.
*******************************************************************************/
package QuoddFeed.util; 

import java.lang.management.*;


/////////////////////////////////////////////////////////////////
// 
//                  c l a s s   C P U
//
/////////////////////////////////////////////////////////////////
public class CPU
{
   private static double _nS = 1.0 / 1000000000.0;

   /////////////////////
   // Constructor / Destructor
   /////////////////////
   public CPU() { ; }


   /////////////////////
   // Access
   /////////////////////
   public double Get()
   {
      return _nS * CpuTimeInNanos();
   }

   public long CpuTimeInNanos()
   {
      ThreadMXBean bean;
      long         cpu;
      boolean      bCPU;

      // Get CPU time in nanoseconds

      bean = ManagementFactory.getThreadMXBean();
      bCPU = bean.isCurrentThreadCpuTimeSupported();
      cpu  = bCPU ? bean.getCurrentThreadCpuTime() : 0L;
      return cpu;
   }

   public long UsrTimeInNanos()
   {
      ThreadMXBean bean;
      long         usr;
      boolean      bUsr;

      // Get User time in nanoseconds

      bean = ManagementFactory.getThreadMXBean();
      bUsr = bean.isCurrentThreadCpuTimeSupported();
      usr  = bUsr ? bean.getCurrentThreadUserTime() : 0L;
      return usr; 
   }
}
