/**
 * Copyright 2000 Tacpoint Technologies, Inc.
 * All rights reserved.
 *
 * @author Tacpoint Technologies, Inc.
 * @version 2.0
 * Date created: 4/14/2001
 * Date modified:
 * - 1/2/2000 Initial version  the old version is in com.tacpoint.util
 * - 4/14/2001 Second Version
 *
 * The SequenceNumberManager class retrieves sequence number
 * from database and return it to the calling class.
 */

package com.tacpoint.dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.tacpoint.exception.NoDataFoundException;

public class SequenceNumberManager
{
    public static long getSequenceNumber(String sequenceName, Connection aConnection) throws Exception
    {
            long seqNbr = 0;
            if (sequenceName == null || aConnection == null) throw new NoDataFoundException("The SequenceName or Connection is null.");
            
            String selection_string = "select " + sequenceName.trim() + ".nextval from dual";
            
            ResultSet rs = null;
            PreparedStatement preparedStmt = null;
            try {
        
                if (aConnection != null)
                {
                    preparedStmt = aConnection.prepareStatement(selection_string);
                    preparedStmt.execute();
                    rs = preparedStmt.getResultSet();
                    if (rs.next()) {
                        seqNbr = rs.getLong(1);
                    } else {
                        throw new NoDataFoundException("No result for the sequence name="+sequenceName);
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
            
            return seqNbr;

    }
           
               
            

}
