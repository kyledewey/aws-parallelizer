/*
 * LocalParameters.java
 *
 * Version:
 *     $Id: LocalParameters.java,v 1.1 2012/11/06 01:12:28 kyle Exp $
 *
 * Revisions:
 *      $Log: LocalParameters.java,v $
 *      Revision 1.1  2012/11/06 01:12:28  kyle
 *      Initial revision
 *
 *
 */

import com.amazonaws.*;
import com.amazonaws.auth.*;
import com.amazonaws.services.sqs.*;
import com.amazonaws.services.sqs.model.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;

import java.io.*;
import java.util.*;

/**
 * Parses in parameters from a local file.
 * Slight issue.
 * <code>readRawParameters()</code> is called before we can do
 * any initialization, so it needs to know the filename in advance.
 * @author Kyle Dewey
 */
public abstract class LocalParameters extends Parameters {
    // begin constants
    public static final String QUEUE_APPEND = "queue";
    public static final String DEFAULT_PARAM_FILE = "parameters.txt";
    // end constants

    /**
     * Dud constructor.
     * Will always fail if this is called as-is.
     */
    protected LocalParameters() throws ParameterException {}

    public static void validateNumNodes( int numNodes ) throws ParameterException {
	if ( numNodes <= 0 ) {
	    throw new ParameterException( "Invalid number of nodes: " + numNodes );
	}
    }

    /**
     * Sets the number of nodes to be the given number
     * @param numNodes The new number of nodes to use
     * @throws ParameterException If the number of nodes <= 0
     */
    public void setNumNodes( int numNodes ) throws ParameterException {
	validateNumNodes( numNodes );
	parameters.put( Parameters.NUM_NODES_ID,
			Integer.toString( numNodes ) );
    }

    /**
     * Gets these parameters as user data.
     * This data is intended to be passed to nodes
     * @pre The object is initialized
     */
    public String toUserData() throws ParameterException {
	return Base64.encodeBytes( ( paramLine( Parameters.INPUT_BUCKET_NAME_ID ) + "\n" +
				     paramLine( Parameters.OUTPUT_BUCKET_NAME_ID ) + "\n" +
				     paramLine( Parameters.QUEUE_URL_ID ) + "\n" +
				     paramLine( Parameters.ENVIRONMENT_BUCKET_NAME_ID ) + "\n" +
				     paramLine( Parameters.ENVIRONMENT_PREFIX_ID ) + "\n" +
				     paramLine( Parameters.ANALYSIS_PROGRAM_NAME_ID ) + "\n" +
				     paramLine( Parameters.ACCESS_KEY_ID ) + "\n" +
				     paramLine( Parameters.SECRET_KEY_ID ) + "\n" +
				     paramLine( Parameters.KEY_PAIR_ID ) ).getBytes() );
    }

    /**
     * Starts up instances to perform analysis
     */
    public void startInstances() throws IOException {
	try {
	    AmazonEC2Client ec2 = new AmazonEC2Client( makeCredentials() );
	    Integer numNodes = new Integer( Integer.parseInt( getParam( Parameters.NUM_NODES_ID ) ) );
	    ec2.runInstances( new RunInstancesRequest( getParam( Parameters.IMAGE_ID_ID ),
						       numNodes,
						       numNodes )
			      .withUserData( toUserData() )
			      .withSecurityGroups( getParam( Parameters.SECURITY_GROUP_ID ) )
			      .withKeyName( getParam( Parameters.KEY_PAIR_ID ) )
			      .withInstanceInitiatedShutdownBehavior( getParam( Parameters.SHUTDOWN_BEHAVIOR_ID ) )
			      .withInstanceType( getParam( Parameters.INSTANCE_TYPE_ID ) ) );
	} catch( NumberFormatException e ) {
	    e.printStackTrace();
	    System.err.println( e );
	} catch ( ParameterException e ) {
	    e.printStackTrace();
	    System.err.println( e );
	}
    }
    
    public AWSCredentials makeCredentials() throws ParameterException {
	return new BasicAWSCredentials( getParam( Parameters.ACCESS_KEY_ID ),
					getParam( Parameters.SECRET_KEY_ID ) );
    }

    public static LocalParameters makeParameters() throws ParameterException {
	return makeParameters( DEFAULT_PARAM_FILE );
    }

    /**
     * Makes a new local parameters object
     * @param fileName The name of the file to read from
     * @throws ParameterException If there was an error with the parameters
     */
    public static LocalParameters makeParameters( final String fileName ) 
	throws ParameterException {
	return new LocalParameters() {
	    public void validateParams() throws ParameterException {
		if ( param( Parameters.QUEUE_URL_ID ) == null ) {
		    try {
			String inputName = getParam( Parameters.INPUT_BUCKET_NAME_ID );
			String queueName = inputName + QUEUE_APPEND;
			AWSCredentials credentials = makeCredentials();
			BucketToQueue.bucketToQueue( new AmazonS3Client( credentials ),
						     new AmazonSQSClient( credentials ),
						     inputName,
						     queueName );
			parameters.put( Parameters.QUEUE_URL_ID,
					queueName );
		    } catch ( IOException e ) {
			e.printStackTrace();
			throw new ParameterException( e.toString() );
		    }
		}
		super.validateParams();
	    } // validateParams

	    public String[] readRawParameters() throws IOException {
		Scanner input = new Scanner( new File( fileName ) );
		List< String > retval = new LinkedList< String >();
		while( input.hasNextLine() ) {
		    retval.add( input.nextLine() );
		}
		input.close();
		return retval.toArray( new String[ 0 ] );
	    } // readRawParameters
	};
    }
}
  
    