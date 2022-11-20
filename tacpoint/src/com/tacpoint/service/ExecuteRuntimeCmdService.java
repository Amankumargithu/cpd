
package com.tacpoint.service;

import java.io.*;

/**
 * Executes a runtime command on behalf of the calling program.
 */
public class ExecuteRuntimeCmdService {

  public boolean submitCmd (String[] cmdArray) throws Exception {

     try {

         Process p = Runtime.getRuntime().exec(cmdArray);
         p.waitFor();
         p.getInputStream().close();
         p.getOutputStream().close();
         p.getErrorStream().close();

     }
     catch (Exception e) {

        System.out.println("ExecuteRuntimeCmdService.submitCmd - Error encountered.");
        e.printStackTrace();
        throw e;

     } 

     return true;

  }

}
