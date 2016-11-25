/**
 * Copyright 2014-2016 by Oracle.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Oracle.
 */


package iwatani;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * https://ganeshpachpind.wordpress.com/2014/02/28/stanford-named-entity-recognizer/
 *
 * @author Dustin Garvey
 */
public class NERExample {
    
    
    public static void main( String[] arguments ) throws Exception {
        
        String modelPath = "/Users/dustingarvey/Documents/B/Bodamer Roger/stevenavery/scratch/2016-11-21 - Ingestion and Metatdata Prototyping/stanford-ner-2015-12-09/classifiers/english.muc.7class.distsim.crf.ser.gz";
        
        File file = new File( "/Users/dustingarvey/Documents/B/Bodamer Roger/stevenavery-output/texts/documentsenteredintoevidence-Report-Reviewed-by-Detective-ONeill/1.txt" );
        BufferedReader reader = new BufferedReader( new FileReader( file ) );
        StringBuilder builder = new StringBuilder( );
        String line = reader.readLine( );
        while( line != null ) {
            builder.append( line );
            line = reader.readLine( );
        }
        reader.close( );
        
        LinkedHashMap< String, LinkedHashSet< String > > result = identifyNER( builder.toString( ), modelPath );
        
    }
    
    
    public static LinkedHashMap <String,LinkedHashSet<String>> identifyNER(String text,String model)
 {
 LinkedHashMap <String,LinkedHashSet<String>> map=new <String,LinkedHashSet<String>>LinkedHashMap();
 String serializedClassifier =model;
 System.out.println(serializedClassifier);
 CRFClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
 List<List<CoreLabel>> classify = classifier.classify(text);
 for (List<CoreLabel> coreLabels : classify)
 {
 for (CoreLabel coreLabel : coreLabels)
 {
 
 String word = coreLabel.word();
 String category = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);
 if(!"O".equals(category))
 {
 if(map.containsKey(category))
 {
 // key is already their just insert in arraylist
 map.get(category).add(word);
 }
 else
 {
 LinkedHashSet<String> temp=new LinkedHashSet<String>();
 temp.add(word);
 map.put(category,temp);
 }
 System.out.println(word+":"+category);
 }
 
 }
 
 }
 return map;
 }

}
