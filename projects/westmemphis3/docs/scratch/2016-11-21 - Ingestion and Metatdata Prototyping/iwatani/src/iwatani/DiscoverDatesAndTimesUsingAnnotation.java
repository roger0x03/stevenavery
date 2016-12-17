/**
 * Copyright 2014-2016 by Dustin Garvey.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Oracle.
 */


package iwatani;


import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.CoreMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;


/**
 *
 * @author Dustin Garvey
 */
public class DiscoverDatesAndTimesUsingAnnotation {
    
    
    
    
    //  PUBLIC METHODS
    
    
    public static void main( String[] arguments ) throws Exception {
        
        //  Suppress red outputs from NLP
        PrintStream err = System.err;
        System.setErr(new PrintStream(new OutputStream() {
            public void write(int b) { }
        }));
        
        //  Get the list of files
        File folder = new File( "/Users/dustingarvey/Documents/B/Bodamer Roger/stevenavery-output/texts" );
        File[] foldersArray = folder.listFiles( );
        List< File > folders = new ArrayList( );
        for( File element : foldersArray ) {
            folders.add( element );
        }
        
        //  Run in single thread
        boolean runInSingleThread = false;
        if( runInSingleThread ) {
            for( int i = 0 ; i < folders.size( ) ; i++ ) {
                File thisFolder = folders.get( i );
                System.out.println( "Process " + thisFolder.getName( ) + 
                        ". (" + ( i + 1 ) + "/" + folders.size( ) + ")" );
                if( thisFolder.getName( ).startsWith( "." ) ) { continue; }
                if( ! thisFolder.isDirectory( ) ) { continue; }
                TimeAnnotater.annotate( folders.get( i ) );
            }
        }
        
        //  Start and wait
        int numThreads = 8;
        TimeAnnotater annotater = new TimeAnnotater( folders, numThreads );
        File file = new File( folder, "0Progress.log" );
        while( ! annotater.isComplete( ) ) {
            Thread.sleep( 60000 );
            if( annotater.exception != null ) {
                throw new RuntimeException( "Exception occured.", annotater.exception );
            }
            String message = annotater.foldersRemaining.size( ) + 
                    " folders remaining.";
            System.out.println( message );
        }
        
        //  Print the patterns and their respective counts
        file = new File( folder, "0AnnotatedTimes.csv" );
        BufferedWriter writer = new BufferedWriter( new FileWriter( file ) );
        writer.write( "Annotation,Count" );
        writer.newLine( );
        for( Entry< String, Integer > entry : 
                annotater.annotationToCount.entrySet( ) ) {
            writer.write( entry.getKey( ) + "," + entry.getValue( ) );
            writer.newLine( );
        }
        writer.close( );
        
    }
    

    
    
}