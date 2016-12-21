/**
 * Copyright 2014-2016 by Oracle.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Oracle.
 */


package iwatani;


import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.util.CoreMap;
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
        Map< String, Set< String > > annotationToRawTimes = new HashMap( );
        Map< String, Set< String > > fileToAnnotations = new HashMap( );
        File root = new File( "/Users/dustingarvey/Documents/B/Bodamer Roger/stevenavery-output/texts" );
        File[] folders = root.listFiles( );
        for( File folder : folders ) {
            if( ! folder.isDirectory( ) ) { continue; }
            File file = new File( folder, "TimeAnnotations.log" );
            if( ! file.exists( ) ) { continue; }
            if( skipFolder( folder ) ) { continue; } // skip certain folders
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

                //  Save the annotation and the file
                String filename = folder.getName( );
                Set< String > annotations = fileToAnnotations.get( filename );
                if( annotations == null ) {
                    annotations = new HashSet( );
                    fileToAnnotations.put( filename, annotations );
                }
                annotations.add( annotation );
                
                //  Save the actual
                Set< String > actuals = annotationToRawTimes.get( annotation );
                if( actuals == null ) { actuals = new HashSet( ); annotationToRawTimes.put( annotation, actuals ); }
                actuals.add( elements[ 0 ] );
                
                //  Read the next line
                line = reader.readLine( );

            }
                
        }
        
        //  Write the file contents
        File file = new File( root, "0DateTimesContainedInEachFile.csv" );
        BufferedWriter writer = new BufferedWriter( new FileWriter( file ) );
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
        
        //  Unpack the annotations
        Map< String, String > rawTimeToAnnotation = new HashMap( );
        for( Entry< String, Set< String > > entry : annotationToRawTimes.entrySet( ) ) {
            String annotation = entry.getKey( );
            for( String actual : entry.getValue( ) ) {
                rawTimeToAnnotation.put( actual, annotation );
            }
        }
        
        //  Get the content surrounding each time
        Map< String, String > rawTimeToContent = new HashMap( );
        Map< String, String > rawTimeToFolder = new HashMap( ); // expect this to not be robust
        for( File folder : folders ) {
            if( ! folder.isDirectory( ) ) { continue; }
            if( skipFolder( folder ) ) { continue; } // skip certain folders
            file = new File( folder, "Context.csv" );
            if( ! file.exists( ) ) { continue; }
            System.out.println( "Extracting context for times in \"" + folder.getName( ) + "\"." );
            BufferedReader reader = new BufferedReader( new FileReader( file ) );
            String line = reader.readLine( );
            while( line != null ) {
                String[] elements = parse( line );
                String rawTime = elements[ 0 ];
                String annotation = rawTimeToAnnotation.get( rawTime );
                if( annotation != null ) {
                    
                    //  Save the context
                    String contentElement = elements[ 1 ].replace( "\n", " " );
                    String content = rawTimeToContent.get( rawTime );
                    if( content != null ) {
                        content = content + " " + contentElement;
                        rawTimeToContent.put( rawTime, content );
                        rawTimeToFolder.put( rawTime, folder.getName( ) );
                    } else {
                        rawTimeToContent.put( rawTime, contentElement );
                        rawTimeToFolder.put( rawTime, folder.getName( ) );
                    }
                    
                }
                line = reader.readLine( );
            }
            reader.close( );
        }
        
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
        
        //  Initialize the facts
        List< Fact > facts = new ArrayList( );
        int index = 0;
        for( Entry< String, String > entry : rawTimeToContent.entrySet( ) ) {
            index++;
            System.out.println( "Creating fact components for time " + index + 
                    " of " + rawTimeToContent.size( ) + "." );
            String content = entry.getValue( ).toLowerCase( );
            boolean hasPerson = false;
            boolean hasLocation = false;
            int numComponents = 1;
            for( String entity : entities ) {
                if( content.contains( entity ) ) {
                    numComponents++;
                    hasPerson = hasPerson || entityToCategory.get( entity ).equals( "person" );
                    hasLocation = hasLocation || entityToCategory.get( entity ).equals( "location" );
                }
            }
            boolean relatedToSA = ( content.contains( "steven" ) || content.contains( "avery" ) );
            if( hasPerson && relatedToSA ) {
                Fact fact = new Fact( );
                String rawTime = entry.getKey( );
                fact.folder = rawTimeToFolder.get( rawTime );
                fact.rawTime = rawTime;
                fact.time = rawTimeToAnnotation.get( rawTime );
                fact.content = entry.getValue( );
                facts.add( fact );
            }
        }
        
        //  Initialize the pipeline 
        AnnotationPipeline pipeline = new AnnotationPipeline( );
        pipeline.addAnnotator( new TokenizerAnnotator( false ) );
        pipeline.addAnnotator( new WordsToSentencesAnnotator( false ) );
        pipeline.addAnnotator( new POSTaggerAnnotator( false ) );
        
        //  Have a go at creating facts 
        for( int i = 0 ; i < facts.size( ) ; i++ ) {
            System.out.println( "Having a go a creating fact " + ( i + 1 ) + 
                    " of " + facts.size( ) + "." );
            Fact fact = facts.get( i );
            Annotation annotation = new Annotation( fact.content );
            pipeline.annotate( annotation );
            for( CoreMap sentence : annotation.get( CoreAnnotations.SentencesAnnotation.class ) ) {
                for (CoreLabel token: sentence.get( TokensAnnotation.class ) ) {
                    String word = token.get( TextAnnotation.class );
                    String pos = token.get( PartOfSpeechAnnotation.class );
                    System.out.println( "Fact " + ( i + 1 ) + ": word = " + word + ", pos = " + pos );
                }
            }
        }
        
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
    
    
    /** Returns true if we should skip some folders. */
    private static boolean skipFolder( File folder ) {
        if( folder.getName( ).startsWith( "jurytrial-Jury-Trial" ) ) { return false; }
        if( folder.getName( ).startsWith( "interviews" ) ) { return false; }
        return true;
    }

    
}
