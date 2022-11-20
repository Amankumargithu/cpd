/**
 * Copyright 2000 Tacpoint Technologies, Inc.
 * All rights reserved.
 *
 * @author  andy katz
 * @author akatz@tacpoint.com
 * @version 1.0
 * Date created: 1/2/2000
 * Date modified:
 * - 1/2/2000 Initial version
 *
 * The SequenceNumberManager class retrieves sequence number
 * from database and return it to the calling class.
 */

package com.tacpoint.util;

import com.tacpoint.dataconnection.DBConnectionManager;
import com.tacpoint.exception.*;

import java.util.*;
import java.sql.*;
import java.io.*;
import java.lang.*;

public class SequenceNumberManager
{
    public int getSequenceNumber(String sequenceName) throws Exception
    {
            int seqNbr = 0;
            
            DBConnectionManager connMgr =  DBConnectionManager.getInstance();
            Connection aConnection = null;
            String selection_string = "select " + sequenceName + ".nextval from dual";
            
            ResultSet rs = null;
            PreparedStatement preparedStmt = null;
            try {
        
                aConnection = connMgr.getConnection("oracle",1000);
        
                if (aConnection != null)
                {
                    preparedStmt = aConnection.prepareStatement(selection_string);
                    preparedStmt.execute();
                    rs = preparedStmt.getResultSet();
                    if (rs.next()) {
                        seqNbr = rs.getInt(1);
                    } else {
                        throw new NoDataFoundException("No ID for the sequence name.");
                    }
                }
            } catch (Exception ae)
            {
                throw new NoDataFoundException("No ID for the sequence name.");
            } finally {
                if (preparedStmt!= null) 
                    try { preparedStmt.close(); } catch (SQLException ex) { }
                if (rs != null) 
                    try { rs.close(); } catch (SQLException ex) { }
            }
            
            connMgr.freeConnection("oracle", aConnection);
            
            return seqNbr;

    }
           
               
            

}
