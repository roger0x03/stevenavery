/**
 * Copyright 2014-2016 by Oracle.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Oracle.
 */


package lochernhead;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;


/**
 * Analyzes all of the primary sources for a specific case.
 *
 * @author Dustin Garvey
 */
public class CaseAnalyzer {
    
    
    
    
    //  VARIABLES
    
    
    private static TestimonyTypeClassifier typeClassifier = null;
    
    
    
    //  CONSTRUCTORS
    
    
    /** Analyzes the primary sources for the supplied case. */
    public CaseAnalyzer( String folderName, Set< String > pKeys, 
            Set< String > dKeys ) throws Exception {
        
        //  Trains the document type classifier if necessary
        if( typeClassifier == null ) {
            System.out.println( "Training testimony type classifier." );
            typeClassifier = new TestimonyTypeClassifier( );
        }
        
        //  Print the progress
        System.out.println( "Analyzing primary sources for \"" + folderName + "\"." );
        
        //  Create the output directory if necessary
        File inputFolder = new File( LochernheadConstants.root, folderName );
        File outputFolder = new File( LochernheadConstants.root, folderName + " - Output" );
        if( !outputFolder.exists( ) ) { outputFolder.mkdir( ); }
        
        //  Annotate each of the text files
        double pFractionTotal = 0;
        double dFractionTotal = 0;
        Map< File, Integer > fileToSwitches = new TreeMap( );
        File[] files = inputFolder.listFiles( );
        for( int i = 0 ; i < files.length ; i++ ) {
            
            //  Skip this file if necessary
            File file = files[ i ];
            String filename = file.getName( );
            if( !filename.endsWith( "txt" ) ) { continue; }
            
            //  Print the progress
            System.out.println( "Annotating file " + ( i + 1 ) + " of " + files.length + "." );
            
            //  Annotate the prosecutor and defence segments and save the result
            String type = typeClassifier.classify( file );
            TestimonyLineReader reader = new TestimonyLineReader( file, true );
            List< String > lines = reader.getLines( );
            PDTextAnnotater pdAnnotater = new PDTextAnnotater( type, pKeys, dKeys, lines );
            pFractionTotal = pFractionTotal + pdAnnotater.pFraction;
            dFractionTotal = dFractionTotal + pdAnnotater.dFraction;
            if( type.equals( TestimonyTypeClassifier.typeDialogue ) ) {
                fileToSwitches.put( file, pdAnnotater.switches );
            }
            String suffix = filename.substring( 0, filename.length( ) - 4 );
            String outputFilename = suffix + " - PD.html";
            BufferedWriter writer = new BufferedWriter( new FileWriter( new File( outputFolder, outputFilename ) ) );
            writer.write( pdAnnotater.html );
            writer.newLine( );
            writer.close( );
            
        }
        
        //  Print the amount of the material supporting each side
        System.out.println( "-----------------------------------------------" );
        System.out.println( "Case: \"" + folderName + "\"" );
        System.out.println( "-----------------------------------------------" );
        System.out.println( "Content Balance:" );
        double fractionTotal = dFractionTotal + pFractionTotal;
        double pFraction = Math.round( 100. * ( pFractionTotal / fractionTotal ) );
        System.out.println( "Prosecution = " + ( int ) pFraction+ "%" );
        double dFraction = Math.round( 100. * ( dFractionTotal / fractionTotal ) );
        System.out.println( "Defense = " + ( int ) dFraction + "%" );
        System.out.println( "-----------------------------------------------" );
        
        //  Print the files and their attribution switches
        System.out.println( "-----------------------------------------------" );
        System.out.println( "Case,AttributionSwitches,Filename" );
        for( Entry< File, Integer > entry : fileToSwitches.entrySet( ) ) {
            String filename = entry.getKey( ).getName( );
            Integer switches = entry.getValue( );
            System.out.println( folderName + "," + switches + "," + filename );
        }
        System.out.println( "-----------------------------------------------" );
        
        
    }
    
    
    
    
    //  PUBLIC METHODS
    
    
    

}
