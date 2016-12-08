/**
 * Copyright 2014-2016 by Oracle.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Oracle.
 */


package iwatani;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 *
 * @author Dustin Garvey
 */
public class CollectTimeAnnotations {
    
    
    public static void main( String[] arguments ) throws Exception {
        
        //  Look at each file
        Map< String, Integer > annotationToCount = new HashMap( );
        Map< String, Set< String > > fileToAnnotations = new HashMap( );
        File root = new File( "/Users/dustingarvey/Documents/B/Bodamer Roger/stevenavery-output/texts" );
        File[] folders = root.listFiles( );
        for( File folder : folders ) {
            if( ! folder.isDirectory( ) ) { continue; }
            File file = new File( folder, "TimeAnnotations.log" );
            if( ! file.exists( ) ) { continue; }
            BufferedReader reader = new BufferedReader( new FileReader( file ) );
            String line = reader.readLine( );
            while( line != null ) {
                String[] elements = line.split( "," );
                for( int i = 1 ; i < elements.length ; i++ ) {
                    
                    //  Only consider dates for now
                    String annotation = elements[ i ];
                    if( annotation.length( ) >= 10 ) { // keep only dates for now
                        
                        //  Skip some dates that aren't useful
                        annotation = annotation.substring( 0, 10 );
                        String[] components = annotation.split( "-" );
                        if( components.length != 3 ) { continue; }
                        if( components[ 0 ].length( ) != 4 ) { continue; }
                        if( components[ 1 ].length( ) != 2 ) { continue; }
                        if( components[ 2 ].length( ) != 2 ) { continue; }
                        if( components[ 1 ].equals( "XX" ) && components[ 2 ].equals( "XX" ) ) { continue; }
                        if( components[ 0 ].equals( "XXXX" ) && components[ 2 ].equals( "XX" ) ) { continue; }
                        try {
                            int year = Integer.parseInt( components[ 0 ] );
                            if( ( year < 1900 ) || ( year > 2020 ) ) { continue; }
                        } catch( Exception exception ) { /** its not a fully described year */ }
                        
                        //  Add the proper millenia
                        if( components[ 0 ].startsWith( "XX" ) && ( ! components[ 0 ].endsWith( "XX" ) ) ) {
                            try {
                                int lastTwoDigits = Integer.parseInt( components[ 0 ].substring( 2 ) );
                                if( lastTwoDigits > 20 ) {
                                    annotation = "19" + annotation.substring( 2 );
                                } else {
                                    annotation = "20" + annotation.substring( 2 );
                                }
                            } catch( Exception exception ) { System.out.println( "Unexpected exception for " + components[ 0 ] + "." ); }
                        }
                        
                    //  We aren't considering this annotation yet
                    } else { continue; }
                    
                    //  Update the count
                    Integer count = annotationToCount.get( annotation );
                    if( count != null ) {
                        annotationToCount.put( annotation, count + 1 );
                    } else {
                        annotationToCount.put( annotation, 1 );
                    }
                    
                    //  Save the annotation and the file
                    String filename = folder.getName( );
                    Set< String > annotations = fileToAnnotations.get( filename );
                    if( annotations == null ) {
                        annotations = new HashSet( );
                        fileToAnnotations.put( filename, annotations );
                    }
                    annotations.add( annotation );
                    
                }
                
                //  Read the next line
                line = reader.readLine( );
                
            }
        }
        
        //  Write the results
        File file = new File( root, "0TimeAnnotationCounts.csv" );
        BufferedWriter writer = new BufferedWriter( new FileWriter( file ) );
        writer.write( "Annotation,Count" );
        writer.newLine( );
        for( Entry< String, Integer > entry : annotationToCount.entrySet( ) ) {
            writer.write( entry.getKey( ) + "," + entry.getValue( ) );
            writer.newLine( );
        }
        writer.close( );
        
        //  Write the file contents
        file = new File( root, "0DatesContainedInEachFile.csv" );
        writer = new BufferedWriter( new FileWriter( file ) );
        writer.write( "File,Date" );
        writer.newLine( );
        for( Entry< String, Set< String > > entry : fileToAnnotations.entrySet( ) ) {
            String filename = entry.getKey( );
            for( String annotation : entry.getValue( ) ) {
                writer.write( filename + "," + annotation );
                writer.newLine( );
            }
        }
        writer.close( );
        
    }

    
}
