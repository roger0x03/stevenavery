/**
 * Copyright 2014-2016 by Dustin Garvey.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Oracle.
 */


package iwatani;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Utility methods.
 *
 * @author Dustin Garvey
 */
public class IwataniUtils {
    
    
    /** Copies in to out. */
    public static void copy( File in, File out ) throws Exception {
        
        //  Copy the bits
        InputStream inStream = new FileInputStream( in );
        OutputStream outStream = new FileOutputStream( out );
        byte[] buffer = new byte[ 1024 ];
        int read;
        while( ( read = inStream.read( buffer ) ) > 0 ) {
            outStream.write( buffer, 0, read );
        } 
 
        //  Close the IO
        inStream.close( );
        outStream.close( );
        
    }
    
    
    /** Deletes the directory and its contents. */
    public static void deleteDirectory( File directory ) {
        
        //  Delete the directory contents
        File[] contents = directory.listFiles( );
        for( File file : contents ) {
            if( file.isDirectory( ) ) {
                deleteDirectory( file );
            } else {
                file.delete( );
            }
        }
        
        //  Remove the directory
        directory.delete( );
        
    }
    

}
