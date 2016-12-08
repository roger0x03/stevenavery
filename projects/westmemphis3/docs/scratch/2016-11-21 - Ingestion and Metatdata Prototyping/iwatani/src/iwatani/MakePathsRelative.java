/**
 * Copyright 2014-2016 by Dustin Garvey.
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
import java.util.Set;


/**
 *
 * @author Dustin Garvey
 */
public class MakePathsRelative {
    
    
    
    
    //  PUBLIC METHODS
    
    
    public static void main( String[] arguments ) throws Exception {
        
        //  Point to the two folders
        File flattenedFolder = new File( "/Users/dustingarvey/Documents/B/Bodamer Roger/stevenavery-output/pdfs" );
        File unflattenedFolder = new File( "/Users/dustingarvey/Documents/B/Bodamer Roger/stevenavery_old" );
        
        //  Get the flattened files and the file names
        File[] flattenedFiles = flattenedFolder.listFiles( );
        Set< String > flattenedFilenames = new HashSet( );
        for( File file : flattenedFiles ) {
            flattenedFilenames.add( file.getName( ) );
        }
        
        //  Map the flattened file names to the original files
        Map< String, File > flattenedFilenameToFile = new HashMap( );
        learnMappings( unflattenedFolder, flattenedFilenames, flattenedFilenameToFile );
        
        for( String filename : flattenedFilenames ) {
            if( ! flattenedFilenameToFile.containsKey( filename ) ) {
                System.out.println( filename );
            }
        }
        
        //  Convert the dates metadata file
        File ioFolder = new File( "/Users/dustingarvey/Documents/B/Bodamer Roger/stevenavery-output/texts" );
        File inputFile = new File( ioFolder, "0DatesContainedInEachFile.csv" );
        File outputFile = new File( ioFolder, "0DatesContainedInEachFile-RelativePaths.csv" );
        BufferedReader reader = new BufferedReader( new FileReader( inputFile ) );
        BufferedWriter writer = new BufferedWriter( new FileWriter( outputFile ) );
        String line = reader.readLine( );
        writer.write( line );
        writer.newLine( );
        line = reader.readLine( );
        while( line != null ) {
            String[] elements = line.split( "," );
            String flattenedFilename = elements[ 0 ] + ".pdf";
            File file = flattenedFilenameToFile.get( flattenedFilename );
            String path = file.getAbsolutePath( );
            path = path.substring( unflattenedFolder.getAbsolutePath( ).length( ) + 1 );
            String outputLine = path + ",";
            for( int i = 1 ; i < elements.length ; i++ ) {
                outputLine = outputLine + elements[ i ];
                if( i != ( elements.length - 1 ) ) {
                    outputLine = outputLine + ",";
                }
            }
            writer.write( outputLine );
            writer.newLine( );
            line = reader.readLine( );
        }
        reader.close( );
        writer.close( );
        
        //  Convert the named entity metadata file
        inputFile = new File( ioFolder, "0NamedEntitiesContainedInEachFile.csv" );
        outputFile = new File( ioFolder, "0NamedEntitiesContainedInEachFile-RelativePaths.csv" );
        reader = new BufferedReader( new FileReader( inputFile ) );
        writer = new BufferedWriter( new FileWriter( outputFile ) );
        line = reader.readLine( );
        writer.write( line );
        writer.newLine( );
        line = reader.readLine( );
        while( line != null ) {
            String[] elements = line.split( "," );
            String flattenedFilename = elements[ 0 ] + ".pdf";
            File file = flattenedFilenameToFile.get( flattenedFilename );
            String path = file.getAbsolutePath( );
            path = path.substring( unflattenedFolder.getAbsolutePath( ).length( ) + 1 );
            String outputLine = path + ",";
            for( int i = 1 ; i < elements.length ; i++ ) {
                outputLine = outputLine + elements[ i ];
                if( i != ( elements.length - 1 ) ) {
                    outputLine = outputLine + ",";
                }
            }
            writer.write( outputLine );
            writer.newLine( );
            line = reader.readLine( );
        }
        reader.close( );
        writer.close( );
        
    }
    
    
    
    
    //  PRIVATE METHODS
    
    
    /** Learn the mappings. */
    private static void learnMappings( File file, 
            Set< String > flattenedFilenames, 
            Map< String, File > flattenedFilenameToFile ) { 
        
        //  Look at a file
        if( ! file.isDirectory( ) ) {
            
            //  Try the simple filename match
            String filename = file.getName( );
            if( flattenedFilenames.contains( filename ) ) {
                flattenedFilenameToFile.put( filename, file );
                return;
            }
            
            //  Try the one level ones
            String folderName = file.getParentFile( ).getName( );
            String flattenedFilename = folderName + "-" + filename;
            if( flattenedFilenames.contains( flattenedFilename ) ) {
                flattenedFilenameToFile.put( flattenedFilename, file );
                return;
            }
            
            //  Try the deeper one
            String otherFolderName = file.getParentFile( ).getParentFile( ).getName( );
            flattenedFilename = otherFolderName + "-" + folderName + "-" + filename;
            if( flattenedFilenames.contains( flattenedFilename ) ) {
                flattenedFilenameToFile.put( flattenedFilename, file );
                return;
            }
        
        //  Look at the folder contents
        } else {
            File[] files = file.listFiles( );
            for( File thisFile : files ) {
                learnMappings( thisFile, flattenedFilenames, 
                        flattenedFilenameToFile );
            }
        }
        
    }
    
    
}