/**
 * Copyright 2014-2016 by Dustin Garvey.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Oracle.
 */


package iwatani;


import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;


/**
 * Ingests a list of PDFs in multiple threads until done.
 *
 * @author Dustin Garvey
 */
public class TimeAnnotater {
    
    
    
    
    //  VARIABLES
    
    
    public Exception exception = null; // tracks exceptions
    
    private Iterator< File > folderIterator; // files
    public Set< File > foldersRemaining = new HashSet( ); // folders that remain
    public static Map< File, Set< String > > folderToAnnotations = new HashMap( );
    private boolean isComplete = false; // finished?
    public int numCompleted = 0; // number of completed 
    private int numFolders; // number of folders
    public static Map< String, Integer > annotationToCount = new HashMap( );
    
    
    
    //  CONSTRUCTORS
    
    
    /** Ingests each file in the specified number of threads. */
    public TimeAnnotater( List< File > folders, int numThreads ) {
        
        //  Create and save the folder iterator
        folderIterator = folders.iterator( );
        numFolders = folders.size( );
        foldersRemaining.addAll( folders );
        
        //  Check to see if we should auto finish or we have too many threads
        //  for what we need
        numThreads = Math.min( numThreads, folders.size( ) );
        if( folders.size( ) == 0 ) {
            isComplete = true;
        }
        
        //  Create the list of indices
        List< Integer > indexList = new ArrayList( );
        for( int i = 0 ; i < folders.size( ) ; i++ ) {
            indexList.add( i );
        }
        
        //  Create index iterator and spin up the specified number of threads
        for( int i = 0 ; i < numThreads ; i++ ) {
            File folder = folderIterator.next( );
            AnnotateThread thread = new AnnotateThread( folder );
            thread.start( );
        }
        
    }
    
    
    
    
    //  PUBLIC METHODS
    
    
    /** Annotates the files in the folder. */
    public static void annotate( File folder ) throws Exception {
        
        //  Initialize the pipeline 
        AnnotationPipeline pipeline = new AnnotationPipeline( );
        pipeline.addAnnotator( new TokenizerAnnotator( false ) );
        pipeline.addAnnotator( new WordsToSentencesAnnotator( false ) );
        pipeline.addAnnotator( new POSTaggerAnnotator( false ) );
        String annotationRuleFolder = "/Users/dustingarvey/Documents/B/Bodamer Roger/libs/stanford-corenlp-full-2016-10-31/sutime";
        pipeline.addAnnotator( new TimeAnnotator( annotationRuleFolder, new Properties( ) ) );
        
        //  Load all the document text
        if( ! folder.isDirectory( ) ) { return; }
        File[] files = folder.listFiles( );
        if( files == null ) { return; }
        StringBuilder builder = new StringBuilder( );
        for( File file : files ) {

            //  Skip non-text files
            if( ! file.getName( ).toLowerCase( ).endsWith( ".txt" ) ) { 
                continue; 
            }

            //  Read the file
            BufferedReader reader = new BufferedReader( new FileReader( file ) );
            StringBuilder fileBuilder = new StringBuilder( );
            String line = reader.readLine( );
            while( line != null ) {
                fileBuilder.append( line );
                line = reader.readLine( );
            }
            reader.close( );

            //  Add the file text
            String text = fileBuilder.toString( );
            builder.append( text );

        }

        //  Get the time annotations
        String text = builder.toString( );
        Set< String > patterns = new HashSet( );
        Set< String > actuals = new HashSet( );
        Annotation annotation = new Annotation( text );
        pipeline.annotate( annotation );
        List< CoreMap > timeAnnotations = annotation.get( TimeAnnotations.TimexAnnotations.class );
        for( CoreMap timeAnnotation : timeAnnotations ) {
            String actual = timeAnnotation.toString( );
            String label = timeAnnotation.get( TimeExpression.Annotation.class ).getTemporal( ).toString( );
            patterns.add( actual + "," + label );
            actuals.add( actual );
        }
        
        //  Unpack the sentences
        List< String > sentences = new ArrayList( );
        for( CoreMap sentenceAnnotation : annotation.get( CoreAnnotations.SentencesAnnotation.class ) ) {
            String sentence = sentenceAnnotation.toString( );
            sentences.add( sentence );
        }
        
        //  Collect all of the sentences that surround times
        Map< String, List< String > > actualToSentences = new HashMap( );
        for( int i = 0 ; i < sentences.size( ) ; i++ ) {
            String sentence = sentences.get( i );
            for( String actual : actuals ) {
                if( sentence.contains( actual ) ) {
                    
                    //  Get the list of sentences associated with this tag
                    List< String > context = actualToSentences.get( actual );
                    if( context == null ) { 
                        context = new ArrayList( );
                        actualToSentences.put( actual, context );
                    }
                    
                    //  Add the sentences before, at and after the sentence
                    try { context.add( sentences.get( i - 2 ).replace( ",", " " ) ); } catch( IndexOutOfBoundsException exception ) {}
                    try { context.add( sentences.get( i - 1 ).replace( ",", " " ) ); } catch( IndexOutOfBoundsException exception ) {}
                    context.add( sentence.replace( ",", " " ) );
                    try { context.add( sentences.get( i + 1 ).replace( ",", " " ) ); } catch( IndexOutOfBoundsException exception ) {}
                    try { context.add( sentences.get( i + 2 ).replace( ",", " " ) ); } catch( IndexOutOfBoundsException exception ) {}
                    
                }
            }
        }

        //  Save the patterns and update the counts
        folderToAnnotations.put( folder, patterns );
        for( String pattern : patterns ) {
            Integer count = annotationToCount.get( pattern );
            if( count != null ) {
                annotationToCount.put( pattern, count + 1 );
            } else {
                annotationToCount.put( pattern, 1 );
            }
        }
        
        //  Write the results
        File file = new File( folder, "TimeAnnotations.log" );
        BufferedWriter writer = new BufferedWriter( new FileWriter( file ) );
        writer.write( "Actual,Annotation" );
        writer.newLine( );
        for( String pattern : patterns ) {
            writer.write( pattern );
            writer.newLine( );
        }
        writer.close( );
        
        //  Write the text surrounding an annotation
        file = new File( folder, "Context.csv" );
        writer = new BufferedWriter( new FileWriter( file ) );
        writer.write( "Actual,Sentence" );
        writer.newLine( );
        for( Entry< String, List< String > > entry : actualToSentences.entrySet( ) ) {
            for( String sentence : entry.getValue( ) ) {
                writer.write( entry.getKey( ) + "," + sentence );
                writer.newLine( );
            }
        }
        writer.close( );
        
    }
    
    
    /** Returns true if we've analyzed everything. */
    public boolean isComplete( ) { return isComplete; }
    
    
    
    
    //  PRIVATE METHODS

    
    /** Indicate we've completed ingesting the supplied file and move on. */
    private synchronized void completed( File folder ) {
        
        //  We are done!
        foldersRemaining.remove( folder );
        numCompleted = numCompleted + 1;
        if( numCompleted == numFolders ) {
            isComplete = true;
            return;
        }
        
        //  Spin up another thread if we can 
        if( folderIterator.hasNext( ) ) {
            folder = folderIterator.next( );
            AnnotateThread thread = new AnnotateThread( folder );
            thread.start( );
        }
        
    }
    
    
    /** Indicates we had an exception. */
    private synchronized void hadException( Exception exception ) {
        this.exception = exception;
    }
    
    
    
    
    //  PRIVATE CLASSES
    
    
    /** Thread to ingest one file. */
    private class AnnotateThread extends Thread {
        
        //  VARIABLES
        File folder; // folder with the files
        
        //  CONSTRUCTORS
        public AnnotateThread( File folder ) {
            this.folder = folder;
        }
        
        
        //  PUBLIC METHODS
        @Override public void run( ) throws RuntimeException {
            try {
                annotate( folder );
                completed( folder );
            } catch( Exception exception ) {
                hadException( exception );
                completed( folder );
            }
        }
        
        
    }
    
    
    
}
