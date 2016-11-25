/**
 * Copyright 2014-2016 by Oracle.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Oracle.
 */


package iwatani;


import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.DocumentReaderAndWriter;
import edu.stanford.nlp.util.Triple;
import java.util.List;


/**
 *
 * @author Dustin Garvey
 */
public class NERDemo {
    
    
    public static void main( String[] arguments ) throws Exception {
        
        String serializedClassifier = "/Users/dustingarvey/Documents/B/Bodamer Roger/stevenavery/scratch/2016-11-21 - Ingestion and Metatdata Prototyping/stanford-ner-2015-12-09/classifiers/english.all.3class.distsim.crf.ser.gz";


        AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(serializedClassifier);

        /* For either a file to annotate or for the hardcoded text example, this
           demo file shows several ways to process the input, for teaching purposes.
         */

        /* For the file, it shows (1) how to run NER on a String, (2) how
           to get the entities in the String with character offsets, and
           (3) how to run NER on a whole file (without loading it into a String).
         */
        String file = "/Users/dustingarvey/Documents/B/Bodamer Roger/stevenavery-output/texts/documentsenteredintoevidence-Report-Reviewed-by-Detective-ONeill/1.txt";
        String fileContents = IOUtils.slurpFile(file);
        List<List<CoreLabel>> out = classifier.classify(fileContents);
        for (List<CoreLabel> sentence : out) {
          for (CoreLabel word : sentence) {
            System.out.print(word.word() + '/' + word.get(CoreAnnotations.AnswerAnnotation.class) + ' ');
          }
          System.out.println();
        }

        System.out.println("---");
        out = classifier.classifyFile(file);
        for (List<CoreLabel> sentence : out) {
          for (CoreLabel word : sentence) {
            System.out.print(word.word() + '/' + word.get(CoreAnnotations.AnswerAnnotation.class) + ' ');
          }
          System.out.println();
        }

        System.out.println("---");
        List<Triple<String, Integer, Integer>> list = classifier.classifyToCharacterOffsets(fileContents);
        for (Triple<String, Integer, Integer> item : list) {
          System.out.println(item.first() + ": " + fileContents.substring(item.second(), item.third()));
        }
        System.out.println("---");
        System.out.println("Ten best entity labelings");
        DocumentReaderAndWriter<CoreLabel> readerAndWriter = classifier.makePlainTextReaderAndWriter();
        classifier.classifyAndWriteAnswersKBest(file, 10, readerAndWriter);

        System.out.println("---");
        System.out.println("Per-token marginalized probabilities");
        
        classifier.printProbs(file, readerAndWriter);

        // -- This code prints out the first order (token pair) clique probabilities.
        // -- But that output is a bit overwhelming, so we leave it commented out by default.
        // System.out.println("---");
        // System.out.println("First Order Clique Probabilities");
        // ((CRFClassifier) classifier).printFirstOrderProbs(args[1], readerAndWriter);
        
    }

}
