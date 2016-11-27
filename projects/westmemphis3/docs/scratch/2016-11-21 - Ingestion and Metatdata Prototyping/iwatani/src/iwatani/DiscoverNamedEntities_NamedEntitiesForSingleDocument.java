/**
 * Copyright 2014-2016 by Dustin Garvey.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Oracle.
 */


package iwatani;


import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 *
 * @author Dustin Garvey
 */
public class DiscoverNamedEntities_NamedEntitiesForSingleDocument {
    
    
    
    
    //  PUBLIC METHODS
    
    
    /** Gets all of the named entities in the loaded files. */
    public static void main( String[] arguments ) throws Exception {
        
        //  Load the named entity classifer
        String modelPath = "/Users/dustingarvey/Documents/B/Bodamer Roger/stevenavery/scratch/2016-11-21 - Ingestion and Metatdata Prototyping/stanford-ner-2015-12-09/classifiers/english.muc.7class.distsim.crf.ser.gz";
        CRFClassifier< CoreLabel > classifier = 
                CRFClassifier.getClassifierNoExceptions( modelPath );
        
        //  Have a look at each file
        File folder = new File( "/Users/dustingarvey/Documents/B/Bodamer Roger/stevenavery-output/texts" );
        File[] subfolders = folder.listFiles( );
        List< String > totalLines = new ArrayList( );
        totalLines.add( "Folder,Word,Category,Count,FileCount" );
        for( int i = 0 ; i < subfolders.length ; i++ ) {
            
            //  Print the progress
            File subfolder = subfolders[ i ];
            System.out.println( "Processing " + subfolder.getName( ) + ". (" + 
                    ( i + 1 ) + "/" + subfolders.length + ")" );
            
            //  Collect the results for each file
            if( ! subfolder.isDirectory( ) ) { continue; }
            File[] files = subfolder.listFiles( );
            if( files == null ) { continue; }
            List< String > lines = new ArrayList( );
            lines.add( "Filename,Word,Category" );
            for( File file : files ) {
                
                //  Skip non-text files
                if( ! file.getName( ).toLowerCase( ).endsWith( ".txt" ) ) { 
                    continue; 
                }
                
                //  Read the text
                BufferedReader reader = new BufferedReader( new FileReader( file ) );
                StringBuilder builder = new StringBuilder( );
                String line = reader.readLine( );
                while( line != null ) {
                    builder.append( line );
                    line = reader.readLine( );
                }
                reader.close( );
                
                //  Extract and manipulate the text
                String text = builder.toString( );
                text = text.replace( ".", " " );
                text = text.replace( "-", " " );
                text = text.replace( "&", " " );
                text = text.replace( "'", " " );
                text = text.replace( "$", " " );
                text = text.replace( "?", " " );
                
                //  Detect entities and save them
                List< List< CoreLabel > > results = classifier.classify( text );
                for( List< CoreLabel > list : results ) {
                    for( CoreLabel label : list ) {
                        String word = label.word( );
                        String category = label.get( CoreAnnotations.AnswerAnnotation.class );
                        if( category.equals( "O" ) ) { continue; }
                        try {
                            int categoryAsInt = Integer.parseInt( category );
                            int k = 2;
                        } catch( Exception exception ) {
                            int k = 3;
                        }
                        lines.add( file.getName( ) + "," + word + "," + category );
                    }
                }
                
            }
            
            //  Save the results to file
            File file = new File( subfolder, "MetadataByFile.csv" );
            BufferedWriter writer = new BufferedWriter( new FileWriter( file ) );
            for( String line : lines ) {
                writer.write( line );
                writer.newLine( );
            }
            writer.close( );
            
            //  Count the word-category combinations
            Map< String, Integer > patternToCount = new HashMap( );
            for( int j = 1 ; j < lines.size( ) ; j++ ) {
                String[] elements = lines.get( j ).split( "," );
                String pattern = elements[ 1 ] + "," + elements[ 2 ];
                pattern = pattern.toLowerCase( );
                Integer count = patternToCount.get( pattern );
                if( count != null ) {
                    patternToCount.put( pattern, count + 1 );
                } else {
                    patternToCount.put( pattern, 1 );
                }
            }
            
            //  Unpack the aggregate subfolder results
            for( Entry< String, Integer > entry : patternToCount.entrySet( ) ) {
                String line = subfolder.getName( ) + "," + entry.getKey( ) + 
                        "," + entry.getValue( ) + "," + files.length;
                totalLines.add( line );
            }
            
        }
        
        //  Write the total lines
        File file = new File( folder, "0Metadata.csv" );
        BufferedWriter writer = new BufferedWriter( new FileWriter( file ) );
        for( String line : totalLines ) {
            writer.write( line );
            writer.newLine( );
        }
        writer.close( );
        
        //  Count the total patterns
        Map< String, Integer > patternToCount = new HashMap( );
        for( int i = 1 ; i < totalLines.size( ) ; i++ ) {
            String[] elements = totalLines.get( i ).split( "," );
            String word = elements[ 1 ];
            String trimmedWord = word.trim( );
            if( trimmedWord.isEmpty( ) ) { continue; }
            if( trimmedWord.length( ) == 1 ) { continue; }
            if( trimmedWord.equals( "mr" ) ) { continue; }
            if( trimmedWord.equals( "mrs" ) ) { continue; }
            if( trimmedWord.equals( "ms" ) ) { continue; }
            String category = elements[ 2 ];
            int count = Integer.parseInt( elements[ 3 ] );
            if( category.equals( "percent" ) ) { continue; }
            if( category.equals( "money" ) ) { continue; }
            String pattern = word + "," + category;
            Integer seenCount = patternToCount.get( pattern );
            if( seenCount != null ) {
                patternToCount.put( pattern, seenCount + count );
            } else {
                patternToCount.put( pattern, count );
            }
        }
        
        //  Write all of the non-singleton patterns
        file = new File( folder, "0Metadata-Consolidated.csv" );
        writer = new BufferedWriter( new FileWriter( file ) );
        writer.write( "Word,Category,Count" );
        writer.newLine( );
        for( Entry< String, Integer > entry : patternToCount.entrySet( ) ) {
            if( entry.getValue( ) == 1 ) { continue; }
            writer.write( entry.getKey( ) + "," + entry.getValue( ) );
            writer.newLine( );
        }
        writer.close( );
        
    }
    
    

}
