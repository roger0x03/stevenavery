/**
 * Copyright 2014-2016 by Oracle.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Oracle.
 */


package lochernhead;


import java.util.HashSet;
import java.util.Set;


/**
 * Analyzes all cases.
 *
 * @author Dustin Garvey
 */
public class AnalyzeAllCases {
    
    
    /** Analyzes all of the cases. */
    public static void main( String[] arguments ) throws Exception {
        
        //  Analyze the Miskelly case
        Set< String > pKeys = new HashSet( );
        pKeys.add( "fogleman" );
        pKeys.add( "davis" );
        Set< String > dKeys = new HashSet( );
        dKeys.add( "stidham" );
        dKeys.add( "crow" );
        CaseAnalyzer analyzer = new CaseAnalyzer( "Case - Misskelley", pKeys, dKeys );
        
        //  Analyze the Echols and Baldwin case
        pKeys = new HashSet( );
        pKeys.add( "fogleman" );
        pKeys.add( "davis" );
        dKeys = new HashSet( );
        dKeys.add( "ford" );
        dKeys.add( "davidson" );
        analyzer = new CaseAnalyzer( "Case - Echols and Baldwin", pKeys, dKeys );
        
    }

}
