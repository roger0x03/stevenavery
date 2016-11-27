/**
 * Copyright 2014-2016 by Dustin Garvey.
 * All rights reserved.
 */


package lochernhead;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


/**
 * Reads lines of testimony text.
 *
 * @author Dustin Garvey
 */
public class TestimonyLineReader {
    
    
    
    
    //  VARIABLES
    
    
    private final List< String > lines; // lines read
    
    
    
    //  CONSTRUCTORS
    
    
    /** Reads the lines in the specified file. */
    public TestimonyLineReader( File file, boolean appendLineNumbers ) 
            throws Exception {
        
        //  Read in all of the lines
        BufferedReader reader = new BufferedReader( new FileReader( file ) ); 
        int lineCount = 0;
        String line = reader.readLine( );
        lines = new ArrayList( );
        while( line != null ) {

            //  Save the line and read the next one if we should
            if( line.length( ) > 0 ) {
                lineCount++;
                line = line.replace( "’", "'" );
                line = line.replace( "’", "'" );
                line = line.replace( "“", "\"" );
                line = line.replace( "”", "\"" );
                line = line.replace( "…", "..." );
                line = line.replace( "—", "-" );
                if( appendLineNumbers ) { line = "[" + lineCount + "] " + line; }
                lines.add( line );
                line = reader.readLine( );

            //  Skip this line otherwise
            } else {
                line = reader.readLine( );
            }

        }

        //  Close the reader
        reader.close( );
        
    }
    
    
    
    
    //  PUBLIC METHODS
    
    
    /** Returns the read lines. */
    public List< String > getLines( ) { return lines; }
    
    

}
