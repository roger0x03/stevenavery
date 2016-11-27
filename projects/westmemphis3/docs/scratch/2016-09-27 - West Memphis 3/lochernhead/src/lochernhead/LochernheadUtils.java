/**
 * Copyright 2014-2016 by Oracle.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Oracle.
 */


package lochernhead;


import java.io.File;
import java.io.FileInputStream;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;


/**
 * Utility methods.
 * 
 * @author Dustin Garvey
 */
public class LochernheadUtils {
    
    
    
    
    //  VARIABLES
    
    
    private static POSTagger posTagger = null; // tags parts of speech
    private static SentenceDetector sentenceDetector = null; // detects sentences
    private static Tokenizer tokenizer = null; // breaks text into words-tokens
    
    
    
    //  PUBLIC METHODS
    
    
    /** Gets the POS tagger. */
    public static POSTagger getPOSTagger( ) throws Exception {
        
        //  Load the tagger if necessary
        if( posTagger == null ) {
            File modelFile = new File( LochernheadConstants.nlpModelRoot, "POSTagger.bin" );
            FileInputStream inputStream = new FileInputStream( modelFile );
            POSModel posModel = new POSModel( inputStream );
            posTagger = new POSTaggerME( posModel );
        }
        
        //  Return the tagger
        return posTagger;
        
    }
    
    
    /** Returns the sentence detector. */
    public static SentenceDetector getSentenceDetector( ) throws Exception {
        
        //  Load the sentence detector if we should
        if( sentenceDetector == null ) {
            File modelFile = new File( LochernheadConstants.nlpModelRoot, "EnglishSentenceDetector.bin" );
            FileInputStream inputStream = new FileInputStream( modelFile );
            SentenceModel sentenceModel = new SentenceModel( inputStream );
            sentenceDetector = new SentenceDetectorME( sentenceModel );
        }
        
        //  Return the sentence detector
        return sentenceDetector;
        
    }
    
    
    /** Returns the tokenizer. */
    public static Tokenizer getTokenizer( ) throws Exception {
        
        //  Load the tokenizer if we need to
        if( tokenizer == null ) {
            File modelFile = new File( LochernheadConstants.nlpModelRoot, "EnglishTokenizer.bin" );
            FileInputStream inputStream = new FileInputStream( modelFile );
            TokenizerModel tokenizerModel = new TokenizerModel( inputStream );
            tokenizer = new TokenizerME( tokenizerModel );
        }
        
        //  Return the tokenizer
        return tokenizer;
        
    }
    
    
    

}
