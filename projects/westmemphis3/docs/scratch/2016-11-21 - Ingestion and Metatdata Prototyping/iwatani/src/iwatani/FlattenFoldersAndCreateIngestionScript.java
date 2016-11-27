/**
 * Copyright 2014-2016 by Dustin Garvey.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Oracle.
 */


package iwatani;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Ingests all the text of a set of PDFs.
 *
 * @author Dustin Garvey
 */
public class FlattenFoldersAndCreateIngestionScript {
    
    
    
    
    //  PUBLIC METHODS
    
    
    /** Does the work. */
    public static void main( String[] arguments ) throws Exception {
        
        //  Point to the folders and execution script
        String sep = "/";
        File root = new File( "/Users/dustingarvey/Documents/B/Bodamer Roger" );
        File inputFolder = new File( root, "stevenavery/" );
        File outputFolder = new File( root, "stevenavery-output/" );
        File pdfsFolder = new File( outputFolder, "pdfs" );
        String shellScriptFilename = "TesseractPDF.sh";
        File inputShellScriptFile = new File( inputFolder, "scratch/2016-11-21 - Ingestion and Metatdata Prototyping/" + shellScriptFilename );
        File outputShellScriptFile = new File( outputFolder, shellScriptFilename );
        
        //  Respawn the output folder
        System.out.println( "Resetting the directories." );
        if( outputFolder.exists( ) ) { IwataniUtils.deleteDirectory( outputFolder ); }
        outputFolder.mkdir( );
        IwataniUtils.copy( inputShellScriptFile, outputShellScriptFile );
        pdfsFolder.mkdir( );
        
        //  Get all of the PDFs in the input folder
        System.out.println( "Finding all PDFs." );
        List< File > pdfFiles = new ArrayList( );
        loadPDFsInDirectory( inputFolder, pdfFiles );
        
        //  Copy each of the files
        System.out.println( "Copying PDFs." );
        File file = new File( outputFolder, "FlattenedFiles.csv" );
        BufferedWriter writer = new BufferedWriter( new FileWriter( file ) );
        writer.write( "Original,Flattened" );
        writer.newLine( );
        for( File in : pdfFiles ) {
            String filename = in.getAbsolutePath( );
            filename = filename.substring( inputFolder.getAbsolutePath( ).length( ) + 1 );
            filename = filename.replace( sep, "-" );
            File out = new File( pdfsFolder, filename );
            IwataniUtils.copy( in, out );
            writer.write( in.getAbsolutePath( ) + "," + out.getAbsolutePath( ) );
            writer.newLine( );
        }
        writer.close( );
        
        //  Load the PDF files we want to process
        System.out.println( "Finding flattened PDFs." );
        pdfFiles.clear( );
        loadPDFsInDirectory( pdfsFolder, pdfFiles );
        
        //  Create the ingestion script
        file = new File( outputFolder, "Ingest.sh" );
        writer = new BufferedWriter( new FileWriter( file ) );
        writer.write( "cd '" + outputFolder.getAbsolutePath( ) + "'" );
        writer.newLine( );
        for( File pdfFile : pdfFiles ) {
            writer.write( "sh TesseractPDF.sh " + pdfFile.getName( ) );
            writer.newLine( );
        }
        writer.close( );
        
    }
    
    
    
    
    //  PRIVATE METHODS
    
    
    /** Loads all of the PDFs in the directory. */
    public static void loadPDFsInDirectory( File directory, 
            List< File > pdfFiles ) {
        
        //  We skip scratch
        if( directory.getName( ).toLowerCase( ).equals( "scratch" ) ) { 
            return; 
        }
        
        //  We skip hidden folders
        if( directory.getName( ).startsWith( "." ) ) {
            return;
        }
        
        //  Look at the directory contents 
        for( File file : directory.listFiles( ) ) {
            if( file.isDirectory( ) ) {
                loadPDFsInDirectory( file, pdfFiles );
            } else if( file.getName( ).toLowerCase( ).endsWith( ".pdf" ) ) {
                pdfFiles.add( file );
            }
        }
        
    }
    
    

}
