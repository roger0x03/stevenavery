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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 *
 * @author Dustin Garvey
 */
public class DiscoverNamedEntities {
    
    
    
    
    //  VARIABLES
    
    
    private static final int minCountForRetention = 11;
    
    
    
    
    //  PUBLIC METHODS
    
    
    /** Gets all of the named entities in the loaded files. */
    public static void main( String[] arguments ) throws Exception {
        
        //  Load the named entity classifer
        String modelPath = "/Users/dustingarvey/Documents/B/Bodamer Roger/stevenavery/scratch/2016-11-21 - Ingestion and Metatdata Prototyping/stanford-ner-2015-12-09/classifiers/english.all.3class.distsim.crf.ser.gz";
        CRFClassifier< CoreLabel > classifier = 
                CRFClassifier.getClassifierNoExceptions( modelPath );
        
        //  Have a look at each file
        File folder = new File( "/Users/dustingarvey/Documents/B/Bodamer Roger/stevenavery-output/texts" );
        File[] subfolders = folder.listFiles( );
        Map< String, Integer > patternToCount = new HashMap( );
        Map< File, Set< String > > subfolderToPatterns = new HashMap( );
        for( int i = 0 ; i < subfolders.length ; i++ ) {
            
            //  Print the progress
            File subfolder = subfolders[ i ];
            System.out.println( "Processing " + subfolder.getName( ) + ". (" + 
                    ( i + 1 ) + "/" + subfolders.length + ")" );
            
            //  Load all the document text
            if( ! subfolder.isDirectory( ) ) { continue; }
            File[] files = subfolder.listFiles( );
            if( files == null ) { continue; }
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
                
                //  Extract and manipulate the text
                String text = fileBuilder.toString( );
                text = text.replaceAll( "[^A-Za-z]", " " );
                //text = text.replaceAll( "[^A-Za-z0-9]", " " );
                
                //  Add the file text
                builder.append( text );
                
            }
            
            //  Detect entities in the file
            String text = builder.toString( );
            Set< String > patterns = new HashSet( );
            List< List< CoreLabel > > results = classifier.classify( text );
            for( List< CoreLabel > list : results ) {
                for( CoreLabel label : list ) {
                    String word = label.word( );
                    String category = label.get( CoreAnnotations.AnswerAnnotation.class );
                    if( category.equals( "O" ) ) { continue; }
                    String trimmedWord = word.trim( );
                    if( trimmedWord.length( ) <= 2 ) { continue; }
                    try { 
                        double trimmedWordAsDouble = Double.parseDouble( trimmedWord );
                        continue; // its a number not a word
                    } catch( Exception exception ) { 
                        //  IT'S NOT A NUMBER -- KEEP IT
                    }
                    String pattern = word + "," + category;
                    pattern = pattern.toLowerCase( );
                    patterns.add( pattern );
                }
            }
            
            //  Save the patterns and update the counts
            subfolderToPatterns.put( subfolder, patterns );
            for( String pattern : patterns ) {
                Integer count = patternToCount.get( pattern );
                if( count != null ) {
                    patternToCount.put( pattern, count + 1 );
                } else {
                    patternToCount.put( pattern, 1 );
                }
            }
            
        }
        
        //  Find the patterns that aren't worth looking into further
        System.out.println( "Summarizing findings across all documents." );
        Collection< String > patternsToForget = new ArrayList( );
        for( Entry< String, Integer > entry : patternToCount.entrySet( ) ) {
            if( entry.getValue( ) < minCountForRetention ) {
                patternsToForget.add( entry.getKey( ) );
            }
        }
        
        //  Remove the non-interesting patterns
        for( String pattern : patternsToForget ) {
            patternToCount.remove( pattern );
        }
        
        //  Unpack the words and categories
        String[] words = new String[ patternToCount.size( ) ];
        String[] categories = new String[ patternToCount.size( ) ];
        int index = 0;
        for( String pattern : patternToCount.keySet( ) ) {
            String[] elements = pattern.split( "," );
            words[ index ] = elements[ 0 ];
            categories[ index ] = elements[ 1 ];
            index++;
        }
        
        //  Find word repeations
        IndexComparator comparator = new IndexComparator( words );
        Integer[] indices = comparator.createIndices( );
        Arrays.sort( indices, comparator );
        Map< String, Set< String > > wordToCategorySet = new HashMap( );
        for( int i = 1 ; i < indices.length ; i++ ) {
            
            //  We have a repeation save the details
            int index1 = indices[ i - 1 ];
            String word1 = words[ index1 ];
            int index2 = indices[ i ];
            String word2 = words[ index2 ];
            if( word1.equals( word2 ) ) {
                Set< String > categorySet = wordToCategorySet.get( word1 );
                if( categorySet == null ) {
                    categorySet = new HashSet( );
                    wordToCategorySet.put( word1, categorySet );
                } 
                categorySet.add( categories[ index1 ] );
                categorySet.add( categories[ index2 ] );
            }
            
        }
        
        //  Keep only the most common categorization
        for( Entry< String, Set< String > > entry : wordToCategorySet.entrySet( ) ) {
            
            //  Get the most commonly classification result for the word
            String word = entry.getKey( );
            int maxCount = 0;
            String mostCommonCategory = "";
            Set< String > categorySet = entry.getValue( );
            for( String category : categorySet ) {
                String pattern = word + "," + category;
                int count = patternToCount.get( pattern );
                if( count > maxCount ) {
                    maxCount = count;
                    mostCommonCategory = category;
                }
            }
            
            //  Delete every other category result
            for( String category : categorySet ) {
                if( ! category.equals( mostCommonCategory ) ) {
                    String pattern = word + "," + category;
                    patternToCount.remove( pattern );
                }
            }
            
        }
        
        //  Write the aggregates 
        File file = new File( folder, "0NamedEntitiesAndCounts.csv" );
        BufferedWriter writer = new BufferedWriter( new FileWriter( file ) );
        writer.write( "Word,Category,Count" );
        writer.newLine( );
        for( Entry< String, Integer > entry : patternToCount.entrySet( ) ) {
            writer.write( entry.getKey( ) + "," + entry.getValue( ) );
            writer.newLine( );
        }
        writer.close( );
        
        //  Write the named entities contained in each file
        file = new File( folder, "0NamedEntitiesContainedInEachFile.csv" );
        writer = new BufferedWriter( new FileWriter( file ) );
        writer.write( "File,Word,Category" );
        writer.newLine( );
        for( Entry< File, Set< String > > entry : subfolderToPatterns.entrySet( ) ) {
            String name = entry.getKey( ).getName( );
            Set< String > patterns = entry.getValue( );
            for( String pattern : patterns ) {
                if( patternToCount.containsKey( pattern ) ) {
                    writer.write( name + "," + pattern );
                    writer.newLine( );
                }
            }
        }
        writer.close( );
        
    }
    
    
    
    
    //  PRIVATE CLASSES
    
    
    /** Lets us sort an array of strings and have the indices. */
    private static class IndexComparator implements Comparator<Integer> {
        
        
        //  VARIABLES
        
        private final String[] x;

        
        //  CONSTRUCTORS
        
        public IndexComparator( String[] x ) { this.x = x; }
        
        
        //  PUBLIC METHODS
        
        public Integer[] createIndices( ) { 
            Integer[] indices = new Integer[ x.length ];
            for ( int i = 0; i < x.length; i++ ) {
                indices[ i ] = i; 
            }
            return indices;
        }
        
        @Override public int compare( Integer i, Integer j ) {
            return x[ i ].compareTo( x[ j ] );
        }
    
        
    }
    
    

    
}
