/*
 * CloudAnalysis.java
 *
 * Version:
 *     $Id: CloudAnalysis.java,v 1.1 2012/11/06 01:12:28 kyle Exp $
 *
 * Revisions:
 *      $Log: CloudAnalysis.java,v $
 *      Revision 1.1  2012/11/06 01:12:28  kyle
 *      Initial revision
 *
 *
 */

import com.amazonaws.*;
import com.amazonaws.auth.*;
import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;

import java.io.*;

/**
 * Moves moves over to the cloud, and starts up clients.
 * Note that it terminates once clients have started
 * @author Kyle Dewey
 */
public class CloudAnalysis {
    /**
     * The main function
     * @param args Command line arguments (parameters file and optional number
     * of nodes to run on)
     */
    public static void main( String[] args ) {
	if ( args.length < 1 ||
	     args.length > 2 ) {
	    System.err.println( "Usage: java CloudAnalysis parameters.txt [numNodes]" );
	    System.exit( 1 );
	}
	
	try {
	    int numNodes = -1;
	    if ( args.length == 2 ) {
		numNodes = Integer.parseInt( args[ 1 ] );
		LocalParameters.validateNumNodes( numNodes );
	    }
	    LocalParameters params = 
		LocalParameters.makeParameters( args[ 0 ] );
	    if ( numNodes != -1 ) {
		params.setNumNodes( numNodes );
	    }
	    params.startInstances();
	} catch ( ParameterException e ) {
	    e.printStackTrace();
	    System.err.println( e );
	} catch ( IOException e ) {
	    e.printStackTrace();
	    System.err.println( e );
	}
    }
}
