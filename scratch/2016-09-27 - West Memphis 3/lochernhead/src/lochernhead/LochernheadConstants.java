/**
 * Copyright 2014-2016 by Dustin Garvey.
 * All rights reserved.
 */


package lochernhead;

import java.io.File;


/**
 * @author Dustin Garvey
 */
public class LochernheadConstants {
    
    /** HTML style for prosecutor-defense testimony. */
    public static String pdStyle = "body { font-family:sans-serif; }" +
            "#defense { background-color : #dfecdf; }" +
            "#prosecution { background-color : #f3d8d8; }";
    
    /** Root directory. */
    public static File root = new File( "/Users/garveyd/Desktop/2016-09-27 - West Memphis 3" );
    
    /** NLP model directory. */
    public static File nlpModelRoot = new File( root, "apache-opennlp-1.6.0/models/" );
    
}
