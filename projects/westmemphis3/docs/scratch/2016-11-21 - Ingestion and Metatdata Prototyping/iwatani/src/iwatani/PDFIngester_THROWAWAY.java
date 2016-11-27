/**
 * Copyright 2014-2016 by Dustin Garvey.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Oracle.
 */


package iwatani;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Ingests a list of PDFs in multiple threads until done.
 *
 * @author Dustin Garvey
 */
public class PDFIngester_THROWAWAY {
    
    
    
    
    //  VARIABLES
    
    
    public Exception exception = null; // tracks exceptions
    
    private Iterator< File > fileIterator; // files
    public Set< File > filesRemaining = new HashSet( ); // folders that remain
    private boolean isComplete = false; // finished?
    public int numCompleted = 0; // number of completed 
    private int numFiles; // number of folders
    
    
    
    
    //  CONSTRUCTORS
    
    
    /** Ingests each file in the specified number of threads. */
    public PDFIngester_THROWAWAY( List< File > files, int numThreads ) {
        
        //  Create and save the folder iterator
        fileIterator = files.iterator( );
        numFiles = files.size( );
        filesRemaining.addAll( files );
        
        //  Check to see if we should auto finish or we have too many threads
        //  for what we need
        numThreads = Math.min( numThreads, files.size( ) );
        if( files.size( ) == 0 ) {
            isComplete = true;
        }
        
        //  Create the list of indices
        List< Integer > indexList = new ArrayList( );
        for( int i = 0 ; i < files.size( ) ; i++ ) {
            indexList.add( i );
        }
        
        //  Create index iterator and spin up the specified number of threads
        for( int i = 0 ; i < numThreads ; i++ ) {
            File folder = fileIterator.next( );
            IngestThread thread = new IngestThread( folder );
            thread.start( );
        }
        
    }
    
    
    
    
    //  PUBLIC METHODS
    
    
    /** Ingests the file. */
    public static void ingest( File file ) throws Exception {
        
        //  Get the root and filename
        File root = file.getParentFile( ).getParentFile( );
        String filename = file.getName( );
        System.out.println( "Ingesting \"" + filename + "\"." );
        
        //  Ingest the file
        String command1 = "sh -c 'cd " + root.getAbsolutePath( ) + "'";
        String command2 = "sh TesseractPDF.sh " + filename;
        String[] commands = { command1, command2 };
        Process process = Runtime.getRuntime( ).exec( commands );
        process.waitFor( );
        int k = 2;
        
    }
    
    
    /** Returns true if we've analyzed everything. */
    public boolean isComplete( ) { return isComplete; }
    
    
    
    
    //  PRIVATE METHODS

    
    /** Indicate we've completed ingesting the supplied file and move on. */
    private synchronized void completed( File file ) {
        
        //  We are done!
        filesRemaining.remove( file );
        numCompleted = numCompleted + 1;
        if( numCompleted == numFiles ) {
            isComplete = true;
            return;
        }
        
        //  Spin up another thread if we can 
        if( fileIterator.hasNext( ) ) {
            file = fileIterator.next( );
            IngestThread thread = new IngestThread( file );
            thread.start( );
        }
        
    }
    
    
    /** Executes a single command. */
    private static String execute( String command ) {
        
        //  Execute the command
        StringBuffer output = new StringBuffer( );
        Process p;
	try {
            p = Runtime.getRuntime( ).exec( command );
            p.waitFor( );
            BufferedReader reader = new BufferedReader( 
                    new InputStreamReader( p.getInputStream( ) ) );
            String line = "";
            while( ( line = reader.readLine( ) ) != null ) {
                output.append(line + "\n");
            }

	} catch( Exception e ) {
            e.printStackTrace( );
        }

        //  Return the result
	return output.toString( );

    }
    
    
    /** Indicates we had an exception. */
    private synchronized void hadException( Exception exception ) {
        this.exception = exception;
    }
    
    
    
    
    //  PRIVATE CLASSES
    
    
    /** Thread to ingest one file. */
    private class IngestThread extends Thread {
        
        //  VARIABLES
        File file; // folder with the sample
        
        //  CONSTRUCTORS
        public IngestThread( File file ) {
            this.file = file;
        }
        
        
        //  PUBLIC METHODS
        @Override public void run( ) throws RuntimeException {
            try {
                ingest( file );
                completed( file );
            } catch( Exception exception ) {
                hadException( exception );
                completed( file );
            }
        }
        
        
    }
    
    
    
}
