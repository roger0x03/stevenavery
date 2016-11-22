/**
 * Copyright 2014-2016 by Dustin Garvey.
 * All rights reserved.
 */


package lochernhead;


import java.io.File;


/**
 *
 * @author Dustin Garvey
 */
public class PrintFolderContents {
    
    
    
    
    //  PUBLIC METHODS
    
    
    /** Prints the contents of the root directory. */
    public static void main( String[] arguments ) throws Exception {
        System.out.println( "Directory,Filename" );
        printDirectoryContents( LochernheadConstants.root );
    }
    
    
    
    
    //  PRIVATE METHODS
    
    
    /** Prints a directory's contents. */
    private static void printDirectoryContents( File directory ) {
        File[] files = directory.listFiles( );
        for( File file : files ) {
            if( file.isDirectory( ) ) {
                printDirectoryContents( file );
            } else {
                System.out.println( directory.toString( ) + "," + file.getName( ) );
            }
        }
    }



    
}
