/**
 * Copyright 2014-2016 by Oracle.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Oracle.
 */


package lochernhead;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;


/**
 * Classifies the type of testimony.
 *
 * @author Dustin Garvey
 */
public class TestimonyTypeClassifier {
    
    
    
    
    //  VARIABLES
    
    
    /** The testimony is dialogue of multiple individuals. */
    public static final String typeDialogue = "Dialogue";
    
    /** The testimony is mostly a single person speaking. */
    public static final String typeMonologue = "Monologue";
    
    /** The testimony type is unknown. */
    public static final String typeUnknown = "Unknown";
    
    
    private static final int numTypeClassifiers = 11; // always must be odd
    private static final String setTrain = "train";
    private static final String setTest = "test";
    double trainFraction = 0.5;
    
    
    private List< RandomForest > typeClassifiers; // set of classifiers
    
    
    
    
    //  CONSTRUCTORS
    
    
    /** Creates a new instance by training new models. */
    public TestimonyTypeClassifier( ) throws Exception {
        
        //  Initilize the list of instances
        Map< String, List< File > > typeToInstances = new HashMap( );
        typeToInstances.put( typeDialogue, new ArrayList( ) );
        typeToInstances.put( typeMonologue, new ArrayList( ) );
        
        //  Read the labels file
        File file = new File( LochernheadConstants.root, "Testimony Type Labels.csv" );
        BufferedReader reader = new BufferedReader( new FileReader( file ) );
        String line = reader.readLine( ); // header
        line = reader.readLine( );
        while( line != null ) {
            line = line.replace( "\"", "" );
            String[] elements = line.split( "," );
            String type = elements[ 2 ];
            File folder = new File( elements[ 0 ] );
            file = new File( folder, elements[ 1 ] );
            typeToInstances.get( type ).add( file );
            line = reader.readLine( );
        }
        
        //  Close the reader
        reader.close( );
        
        //  Create features for each of the instances
        Map< String, List< TestimonyFeatureCreator > > typeToFeatureCreators = new HashMap( );
        typeToFeatureCreators.put( typeDialogue, new ArrayList( ) );
        typeToFeatureCreators.put( typeMonologue, new ArrayList( ) );
        for( Entry< String, List< File > > entry : typeToInstances.entrySet( ) ) {
            String type = entry.getKey( );
            List< File > files = entry.getValue( );
            int count = 0;
            for( File thisFile : files ) {
                
                //  Create and save the features for this file
                count++;
                System.out.println( "Creating features for instance #" + count + 
                        " of type \"" + type + "\"." );
                TestimonyFeatureCreator creator = new TestimonyFeatureCreator( 
                        thisFile );
                typeToFeatureCreators.get( type ).add( creator );
                
            }
        }
        
        //  Train and test several times
        Random random = new Random( 0l );
        typeClassifiers = new ArrayList( );
        List< Double > errorRates = new ArrayList( );
        for( int cycle = 0 ; cycle < numTypeClassifiers ; cycle++ ) {
            
            //  Train and evaluate a model
            System.out.println( "Training and testing classifier #" + ( cycle + 1 ) + "." );
            Map< String, Instances > setToInstances = createInstances( 
                    typeToFeatureCreators, random, trainFraction );
            RandomForest classifier = new RandomForest( );
            classifier.buildClassifier( setToInstances.get( setTrain ) );
            typeClassifiers.add( classifier );
            
            //  Calculate the sum of instance that we got wrong
            Instances testInstances = setToInstances.get( setTest );
            double numWrong = 0;
            for( int i = 0 ; i < testInstances.numInstances( ) ; i++ ) {
                Instance instance = testInstances.get( i );
                double prediction = classifier.classifyInstance( instance );
                double observed = instance.value( 0 );
                if( prediction != observed ) {
                    numWrong++;
                }
            }
            
            //  Save the error rate
            double errorRate = numWrong / ( ( double ) testInstances.numInstances( ) );
            errorRates.add( errorRate );
            
        }
        
        //  Calculates the average accuracy
        double avgAccuracy = 0.;
        for( double errorRate : errorRates ) {
            double accuracy = 1. - errorRate;
            avgAccuracy = avgAccuracy + ( accuracy / ( ( double ) errorRates.size( ) ) );
        }
        
        //  Print the range of the error rates
        avgAccuracy = Math.round( avgAccuracy * 100. ) / 100.;
        System.out.println( "Average testimony type classification of " + 
                avgAccuracy + "." );
        
    }
    
    
    
    
    //  PUBLIC METHODS
    
    
    /** Returns the inferred type of the testimony in the specified file. */
    public String classify( File file ) throws Exception {
        
        //  Create the instance to run
        FastVector attributes = createAttributes( );
        Instances instances = new Instances( setTrain, attributes, 1 );
        instances.setClassIndex( 0 );
        TestimonyFeatureCreator creator = new TestimonyFeatureCreator( file );
        Instance instance = createInstance( instances, typeUnknown, creator );
        
        //  Vote using each model
        int numForMonologue = 0; // monologue = 1 b/c alpha. after dialogue
        for( RandomForest classifier : typeClassifiers ) {
            int vote = ( int ) classifier.classifyInstance( instance );
            numForMonologue = numForMonologue + vote;
        }
        
        //  Return the appropriate result
        double half = 1. / ( ( double ) typeClassifiers.size( ) );
        if( numForMonologue < half ) {
            return typeDialogue;
        } else if( numForMonologue > half ) {
            return typeMonologue;
        } else {
            throw new RuntimeException( "Ties aren't allowed!" );
        }
        
    }
    
    
    
    
    //  PRIVATE METHODS
    
    
    /** Creates the attributes. */
    private FastVector createAttributes( ) {
        FastVector typeValues = new FastVector( );
        typeValues.add( typeDialogue );
        typeValues.add( typeMonologue );
        typeValues.add( typeUnknown );
        FastVector attributes = new FastVector( );
        attributes.add( new Attribute( "type", typeValues ) );
        attributes.add( new Attribute( "lineCount" ) );
        attributes.add( new Attribute( "avgWordsPerLine" ) );
        attributes.add( new Attribute( "colonsPerLine" ) );
        attributes.add( new Attribute( "nounFrequency" ) );
        attributes.add( new Attribute( "properNounFrequency" ) );
        attributes.add( new Attribute( "adjectiveFrequency" ) );
        attributes.add( new Attribute( "pronounFrequency" ) );
        return attributes;
    }
    
    
    /** 
     * Returns a single instance for the supplied instances, testimony type, 
     * and feature creator.
     */
    private Instance createInstance( Instances instances, String type, 
            TestimonyFeatureCreator creator ) {
        Instance instance = new DenseInstance( instances.numAttributes( ) );
        instance.setDataset( instances );
        instance.setValue( 0, type );
        instance.setValue( 1, creator.lineCount );
        instance.setValue( 2, creator.avgWordsPerLine );
        instance.setValue( 3, creator.avgColonsPerLine );
        instance.setValue( 4, creator.nounFrequency );
        instance.setValue( 5, creator.properNounFrequency );
        instance.setValue( 6, creator.adjectiveFrequency );
        instance.setValue( 7, creator.pronounFrequency );
        return instance;
    }
    
    
    /** Returns a sampled training and test set of instances. */
    private Map< String, Instances > createInstances( 
            Map< String, List< TestimonyFeatureCreator > > typeToFeatureCreators, 
            Random random, double trainFraction ) {
        
        //  Create the training and test instance
        int smallestSet = Math.min( 
                typeToFeatureCreators.get( typeDialogue ).size( ), 
                typeToFeatureCreators.get( typeMonologue ).size( ) );
        int numTrain = ( int ) ( trainFraction * ( ( double ) smallestSet ) );
        int numTest = smallestSet - numTrain;
        FastVector attributes = createAttributes( );
        Instances trainInstances = new Instances( setTrain, attributes, numTrain );
        trainInstances.setClassIndex( 0 );
        Instances testInstances = new Instances( setTest, attributes, numTest );
        testInstances.setClassIndex( 0 );
        
        //  Add the instances for each class
        String[] types = { typeMonologue, typeDialogue };
        for( String type : types ) {
            
            //  Add instances for training
            List< TestimonyFeatureCreator > creators = 
                    typeToFeatureCreators.get( type );
            Set< Integer > sampled = new HashSet( );
            while( sampled.size( ) < numTrain ) {
                
                //  Sample an integer and skip if we got this one already
                int index = random.nextInt( creators.size( ) );
                if( sampled.contains( index ) ) { continue; }
                
                //  Create an instance
                TestimonyFeatureCreator creator = creators.get( index );
                Instance instance = createInstance( trainInstances, type, creator );
                trainInstances.add( instance );
                
                //  Remember we sampled this one
                sampled.add( index );
                
                
            }
            
            //  Add instances for test
            while( sampled.size( ) < smallestSet ) {
                
                //  Sample an integer and skip if we got this one already
                int index = random.nextInt( creators.size( ) );
                if( sampled.contains( index ) ) { continue; }
                
                //  Create an instance
                TestimonyFeatureCreator creator = creators.get( index );
                Instance instance = createInstance( testInstances, type, creator );
                testInstances.add( instance );
                
                //  Remember we sampled this one
                sampled.add( index );
                
                
            }
            
        }
        
        //  Package and return the instances
        Map< String, Instances > setToInstances = new HashMap( );
        setToInstances.put( setTrain, trainInstances );
        setToInstances.put( setTest, testInstances );
        return setToInstances;
        
    }
    
    
    
    
}
