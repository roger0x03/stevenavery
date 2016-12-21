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
import java.util.ArrayList;
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
public class CollectTimeAnnotations {
    
    
    public static void main( String[] arguments ) throws Exception {
        
        //  Look at each file
        Map< String, Integer > annotationToCount = new HashMap( );
        Map< String, Set< String > > annotationToActuals = new HashMap( );
        Map< String, Set< String > > fileToAnnotations = new HashMap( );
        File root = new File( "/Users/dustingarvey/Documents/B/Bodamer Roger/stevenavery-output/texts" );
        File[] folders = root.listFiles( );
        for( File folder : folders ) {
            if( ! folder.isDirectory( ) ) { continue; }
            File file = new File( folder, "TimeAnnotations.log" );
            if( ! file.exists( ) ) { continue; }
            System.out.println( "Processing \"" + folder.getName( ) + "\"." );
            BufferedReader reader = new BufferedReader( new FileReader( file ) );
            String line = reader.readLine( );
            while( line != null ) {
                    
                //  Extract dates only
                String[] elements = parse( line );
                String annotation = elements[ 1 ];
                if( ( annotation.contains( "-" ) ) && 
                        ( ! annotation.contains( ":" ) ) && 
                        ( annotation.length( ) >= 10 ) ) { 

                    //  Skip some dates that aren't useful
                    annotation = annotation.substring( 0, 10 );
                    String[] components = annotation.split( "-" );
                    if( components.length != 3 ) { line = reader.readLine( ); continue; }
                    if( components[ 0 ].length( ) != 4 ) { line = reader.readLine( ); continue; }
                    if( components[ 1 ].length( ) != 2 ) { line = reader.readLine( ); continue; }
                    if( components[ 2 ].length( ) != 2 ) { line = reader.readLine( ); continue; }
                    if( components[ 1 ].equals( "XX" ) && components[ 2 ].equals( "XX" ) ) { line = reader.readLine( ); continue; }
                    if( components[ 0 ].equals( "XXXX" ) && components[ 2 ].equals( "XX" ) ) { line = reader.readLine( ); continue; }
                    try {
                        int year = Integer.parseInt( components[ 0 ] );
                        if( ( year < 1900 ) || ( year > 2020 ) ) { line = reader.readLine( ); continue; }
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
                    
                //  Extract only times
                } else if( ( ! annotation.contains( "-" ) ) && 
                        ( annotation.contains( ":" ) ) && 
                        ( annotation.length( ) >= 6 ) ) {
                    annotation = annotation.substring( 0, 6 );
                    
                //  Extract dates and times
                } else if( annotation.contains( "-" ) && 
                        annotation.contains( ":" ) && 
                        ( annotation.length( ) >= 16 ) ) {
                    
                    //  Handle the case where we have a week assignment
                    if( annotation.contains( "W" ) ) {
                        String date = annotation.substring( 0, 10 );
                        if( date.contains( "W" ) ) { line = reader.readLine( ); continue; }
                        String time = annotation.substring( annotation.length( ) - 6 );
                        annotation = date + time;
                    }
                    
                    //  Get what we need
                    annotation = annotation.substring( 0, 16 ); 

                //  We aren't considering this annotation yet
                } else { 
                    line = reader.readLine( );
                    continue; 
                }

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
                
                //  Save the actual
                Set< String > actuals = annotationToActuals.get( annotation );
                if( actuals == null ) { actuals = new HashSet( ); annotationToActuals.put( annotation, actuals ); }
                actuals.add( elements[ 0 ] );
                
                //  Read the next line
                line = reader.readLine( );

            }
                
        }
        
        //  Write the results
        File file = new File( root, "0DateTimeAnnotationCounts.csv" );
        BufferedWriter writer = new BufferedWriter( new FileWriter( file ) );
        writer.write( "Annotation,Count" );
        writer.newLine( );
        for( Entry< String, Integer > entry : annotationToCount.entrySet( ) ) {
            writer.write( entry.getKey( ) + "," + entry.getValue( ) );
            writer.newLine( );
        }
        writer.close( );
        
        //  Write the file contents
        file = new File( root, "0DateTimesContainedInEachFile.csv" );
        writer = new BufferedWriter( new FileWriter( file ) );
        writer.write( "File,DateTime" );
        writer.newLine( );
        for( Entry< String, Set< String > > entry : fileToAnnotations.entrySet( ) ) {
            String filename = entry.getKey( );
            for( String annotation : entry.getValue( ) ) {
                writer.write( filename + "," + annotation );
                writer.newLine( );
            }
        }
        writer.close( );
        
        //  Write the annotations related to each segement of actual text
        file = new File( root, "0DateTimesToActualText.csv" );
        writer = new BufferedWriter( new FileWriter( file ) );
        writer.write( "Actual,DateTime" );
        writer.newLine( );
        for( Entry< String, Set< String > > entry : annotationToActuals.entrySet( ) ) {
            String annotation = entry.getKey( );
            for( String actual : entry.getValue( ) ) {
                writer.write( "\"" + actual + "\",\"" + annotation + "\"" );
                writer.newLine( );
            }
        }
        writer.close( );
        
        //  Unpack the annotatios
        Map< String, String > actualToAnnotation = new HashMap( );
        for( Entry< String, Set< String > > entry : annotationToActuals.entrySet( ) ) {
            String annotation = entry.getKey( );
            for( String actual : entry.getValue( ) ) {
                actualToAnnotation.put( actual, annotation );
            }
        }
        
        //  Write the context for each time annotation
        Map< String, String > timeToContent = new HashMap( );
        file = new File( root, "0DateTimesContexts.csv" );
        writer = new BufferedWriter( new FileWriter( file ) );
        writer.write( "Folder,Annotation,Context"  );
        writer.newLine( );
        for( File folder : folders ) {
            if( ! folder.isDirectory( ) ) { continue; }
            file = new File( folder, "Context.csv" );
            if( ! file.exists( ) ) { continue; }
            System.out.println( "Extracting context for times in \"" + folder.getName( ) + "\"." );
            BufferedReader reader = new BufferedReader( new FileReader( file ) );
            String line = reader.readLine( );
            while( line != null ) {
                String[] elements = parse( line );
                String annotation = actualToAnnotation.get( elements[ 0 ] );
                if( annotation != null ) {
                    
                    //  Write the result
                    String context = elements[ 1 ].replace( "\n", " " );
                    writer.write( "\"" + folder.getName( ) + "\",\"" + 
                            annotation + "\",\"" + context + "\"");
                    writer.newLine( );
                    
                    //  Save the result
                    String content = timeToContent.get( annotation );
                    if( content != null ) { 
                        content = content + " " + context;
                        timeToContent.put( annotation, content );
                    } else {
                        timeToContent.put( annotation, context );
                    }
                    
                }
                line = reader.readLine( );
            }
            reader.close( );
        }
        writer.close( );
        
        //  Read in the named entities
        file = new File( root, "0NamedEntitiesContainedInEachFile.csv" );
        BufferedReader reader = new BufferedReader( new FileReader( file ) );
        String line = reader.readLine( );
        line = reader.readLine( );
        Set< String > entities = new HashSet( );
        Map< String, String > entityToCategory = new HashMap( );
        while( line != null ) {
            String[] elements = line.split( "," );
            String entity = elements[ 1 ];
            String category = elements[ 2 ];
            entities.add( entity );
            entityToCategory.put( entity, category );
            line = reader.readLine( );
        }
        reader.close( );
        
        //  Create the fact contents by getting all of the named entities 
        //  in the lines with the content
        Map< String, String > factComponentsToContent = new HashMap( );
        int index = 0;
        for( Entry< String, String > entry : timeToContent.entrySet( ) ) {
            index++;
            System.out.println( "Creating fact components for time " + index + 
                    " of " + timeToContent.size( ) + "." );
            String factComponents = entry.getKey( ) + "-";
            String content = entry.getValue( ).toLowerCase( );
            boolean hasPerson = false;
            boolean hasLocation = false;
            int numComponents = 1;
            for( String entity : entities ) {
                if( content.contains( entity ) ) {
                    numComponents++;
                    hasPerson = hasPerson || entityToCategory.get( entity ).equals( "person" );
                    hasLocation = hasLocation || entityToCategory.get( entity ).equals( "location" );
                    factComponents = factComponents + entity + "-";
                }
            }
            boolean relatedToSA = ( content.contains( "steven" ) || content.contains( "avery" ) );
            if( hasPerson && relatedToSA ) {
                factComponents = factComponents.substring( 0, factComponents.length( ) - 1 );
                factComponentsToContent.put( factComponents, entry.getValue( ) );
            }
        }
        
        //  Write the fact elements
        file = new File( root, "0FactComponetsToContent.csv" );
        writer = new BufferedWriter( new FileWriter( file ) );
        writer.write( "\"FactComponents\",\"Content\"" );
        writer.newLine( );
        for( Entry< String, String > entry : factComponentsToContent.entrySet( ) ) {
            writer.write( "\"" + entry.getKey( ) + "\",\"" + entry.getValue( ) + "\"" );
            writer.newLine( );
        }
        writer.close( );
        
    }
    
    
    
    
    //  PRIVATE METHODS
    
    
    /** Parses the input line. */
    private static String[] parse( String line ) {
        int quoteCount = 0;
        for( int i = 0 ; i < line.length( ) ; i++ ) {
            if( line.charAt( i ) == '\"' ) {
                quoteCount++;
            } else if( ( line.charAt( i ) == ',' ) && ( quoteCount >= 2 ) ) {
                String element1 = line.substring( 0, i ).replace( "\"", "" );
                String element2 = line.substring( i + 1 ).replace( "\"", "" );
                return new String[] { element1, element2 };
            }
        }
        return line.split( "," );
    }

    
}
