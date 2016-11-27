/**
 * Copyright 2014-2016 by Dustin Garvey.
 * All rights reserved.
 */


package lochernhead;


import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;


/**
 *
 * @author Dustin Garvey
 */
public class TestimonyFeatureCreator {
    
    
    
    
    //  VARIABLES
    
    
    public final int adjectiveCount;
    public final int colonCount;
    public final int lineCount;
    public final int nounCount;
    public final int properNounCount;
    public final int pronounCount;
    public final int wordCount;
    
    public final double adjectiveFrequency;
    public final double avgWordsPerLine;
    public final double avgColonsPerLine;   
    public final double nounFrequency;
    public final double properNounFrequency;
    public final double pronounFrequency;
    
    
    
    
    //  CONSTRUCTORS
    
    
    /** Creates the features for the input lines. */
    public TestimonyFeatureCreator( File file ) throws Exception {
        
        //  Read in the lines without adding the leading line number
        TestimonyLineReader reader = new TestimonyLineReader( file, false );
        List< String > lines = reader.getLines( );
        
        //  Define the incrementally updated features
        int wordCount = 0;
        int adjectiveCount = 0;
        int colonCount = 0;
        int nounCount = 0;
        int properNounCount = 0;
        int pronounCount = 0;
        
        
        //  Update the counts for each line
        Tokenizer tokenizer = LochernheadUtils.getTokenizer( );
        POSTagger posTagger = LochernheadUtils.getPOSTagger( );
        for( String line : lines ) {
            
            //  Count the words
            String[] tokens = tokenizer.tokenize( line );
            wordCount = wordCount + tokens.length; // tokens can not be words, need to fix
            
            //  Tag the words and increment the appropriate counts
            String[] tags = posTagger.tag( tokens );
            for( String tag : tags ) {
                if( tag.equals( "JJ" ) || tag.equals( "JJR" ) || tag.equals( "JJS" ) ) {
                    adjectiveCount++;
                } else if( tag.equals( ":" ) ) {
                    colonCount++;
                } else if( tag.equals( "NN" ) || tag.equals( "NNS" ) ) {
                    nounCount++;
                } else if( tag.equals( "NNP" ) || ( tag.equals( "NNPS" ) ) ) {
                    properNounCount++;
                } else if( tag.equals( "PRP" ) || ( tag.equals( "PRP$" ) ) ) {
                    pronounCount++;
                }
            }
            
        }
        
        //  Save the counts
        this.adjectiveCount = adjectiveCount;
        this.colonCount = colonCount;
        this.lineCount = lines.size( );
        this.nounCount = nounCount;
        this.pronounCount = pronounCount;
        this.properNounCount = properNounCount;
        this.wordCount = wordCount;
        
        //  Save the more complex features
        double wordCountAsDouble = ( double ) wordCount;
        adjectiveFrequency = ( ( double ) adjectiveCount ) / wordCountAsDouble;
        double lineCountAsDouble = ( double ) lineCount;
        avgWordsPerLine = wordCountAsDouble / lineCountAsDouble;
        avgColonsPerLine = ( ( double ) colonCount ) / lineCountAsDouble;
        nounFrequency = ( ( double ) nounCount ) / wordCountAsDouble;
        pronounFrequency = ( ( double ) pronounCount ) / wordCountAsDouble;
        properNounFrequency = ( ( double ) properNounCount ) / wordCountAsDouble;
        
    }
    
    
    
    
}
