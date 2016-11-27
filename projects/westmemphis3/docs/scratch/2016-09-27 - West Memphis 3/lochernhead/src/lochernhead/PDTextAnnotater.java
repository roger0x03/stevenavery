/**
 * Copyright 2014-2016 by Dustin Garvey.
 * All rights reserved.
 */


package lochernhead;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.tokenize.Tokenizer;


/**
 * Annotate a text document as being composed of prosecution-defense sequences.
 *
 * @author Dustin Garvey
 */
public class PDTextAnnotater {
    
    
    
    
    //  VARIABLES 
    
    
    private final int D = -1;
    private final int P = 1;
    private final int UNKNOWN = 0;
    
    public final int switches;
    public final double dFraction;
    public final double pFraction;
    public final String html;
    
    
    
    
    //  CONSTRUCTORS
    
    
    /** Creates annotated HTML with prosecutor-defense sequences. */
    public PDTextAnnotater( String type, Collection< String > pKeys, 
            Collection< String > dKeys, List< String > lines ) 
            throws Exception {
        
        //  Attribute each line based on key content
        List< Integer > attributions = new ArrayList( );
        for( String line : lines ) {
            
            //  Does it contain any prosecutor keys
            boolean isP = false;
            for( String key : pKeys ) {
                if( line.toLowerCase( ).contains( key ) ) {
                    isP = true;
                    break;
                } 
            }
            
            //  Does it contain any defense keys
            boolean isD = false;
            for( String key : dKeys ) {
                if( line.toLowerCase( ).contains( key ) ) {
                    isD = true;
                    break;
                }
            }
            
            //  Attribute to defence or prosecution or neither
            if( isP && ( !isD ) ) {
                attributions.add( P );
            } else if( isD && ( !isP ) ) {
                attributions.add( D );
            } else {
                attributions.add( UNKNOWN );
            }
            
        }
        
        //  Manipulate the attributions, depending on the type of document
        if( type.equals( TestimonyTypeClassifier.typeDialogue ) ) {
            merge( attributions );
        } else if( type.equals( TestimonyTypeClassifier.typeMonologue ) ) {
            allAreFirst( attributions );
        }
        
        //  Count the attributions
        int pCount = 0;
        int dCount = 0;
        Tokenizer tokenizer = LochernheadUtils.getTokenizer( );
        for( int i = 0 ; i < lines.size( ) ; i++ ) {
            int attribution = attributions.get( i );
            if( attribution == P ) {
                String[] tokens = tokenizer.tokenize( lines.get( i ) );
                pCount = pCount + tokens.length;
            } else if( attribution == D ) {
                String[] tokens = tokenizer.tokenize( lines.get( i ) );
                dCount = dCount + tokens.length;
            }
        }
        
        //  Save the attribution fractions
        double attributedTokens = pCount + dCount;
        this.pFraction = ( ( double ) pCount ) / attributedTokens;
        this.dFraction = ( ( double ) dCount ) / attributedTokens;
        
        //  Count the attribution switches
        int switches = 0;
        for( int i = 1 ; i < lines.size( ) ; i++ ) {
            if( attributions.get( i - 1 ) == UNKNOWN ) { continue; }
            if( attributions.get( i ) == UNKNOWN ) { continue; }
            if( attributions.get( i - 1 ) != attributions.get( i ) ) {
                switches++;
            }
        }
        
        //  Save the switches
        this.switches = switches;
        
        //  Start the HTML
        StringBuilder builder = new StringBuilder( );
        builder.append( "<html><head><style>" );
        builder.append( LochernheadConstants.pdStyle );
        builder.append( "</style></head><body>" );
        
        //  Add each of the lines
        for( int i = 0 ; i < lines.size( ) ; i++ ) {
            
            //  Add the appropriate suffix
            int attribution = attributions.get( i );
            if( attribution == D ) {
                builder.append( "<p id = \"defense\">" );
            } else if( attribution == P ) {
                builder.append( "<p id = \"prosecution\">" );
            } else {
                builder.append( "<p>" );
            }
            
            //  Add the paragraph to the HTML
            builder.append( lines.get( i ) );
            builder.append( "</p>" );
            
        }
        
        //  Finish up and set the HTML
        builder.append( "</body></html>" );
        html = builder.toString( );
        
    }
    
    
    
    
    //  PRIVATE METHODS
    
    
    /** All lines are attributed to the first known entry. */
    private void allAreFirst( List< Integer > attributions ) {
        
        //  Get the first known attribution
        int firstKnown = UNKNOWN;
        for( int attribution : attributions ) {
            if( attribution != UNKNOWN ) {
                firstKnown = attribution;
                break;
            }
        }
        
        //  All are the first!
        for( int i = 0 ; i < attributions.size( ) ; i++ ) {
            attributions.set( i, firstKnown );
        }
        
    }
    
    
    /** 
     * Fills in attributions over segments of at most n lines where the 
     * interstitial content does not conflict. 
     */
    private void merge( List< Integer > attributions ) {
        
        //  Go until we are finished
        int i = 0;
        while( i < attributions.size( ) ) {
            
            //  There is no attribution, go to the next one
            int attribution = attributions.get( i );
            if( attribution == UNKNOWN ) { i++; }
            
            //  Find the index of the opposing attribution
            int j = i + 1;
            while( j < attributions.size( ) ) {
                int otherAttribution = attributions.get( j );
                if( ( attribution != otherAttribution ) && 
                        ( otherAttribution != UNKNOWN ) ) {
                    break;
                }
                j++;
            }
            
            //  Fill in all the values up to the other opposing attribution
            for( int k = i + 1 ; k < j ; k++ ) {
                attributions.set( k, attribution );
            }
            
            //  Start where we found the other
            i = j;
            
        }
        
    }
    
    
    
    
}
