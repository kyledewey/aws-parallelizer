/*
 * AWSParameters.java
 *
 * Version:
 *     $Id: AWSParameters.java,v 1.1 2012/11/06 01:12:28 kyle Exp $
 *
 * Revisions:
 *      $Log: AWSParameters.java,v $
 *      Revision 1.1  2012/11/06 01:12:28  kyle
 *      Initial revision
 *
 *
 */

import com.amazonaws.*;
import com.amazonaws.auth.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.sqs.*;
import com.amazonaws.services.sqs.model.*;

import java.io.*;
import java.util.*;

/**
 * Like Parameters, but it features certain helper routines that
 * are specific to AWS.
 * @author Kyle Dewey
 */
public class AWSParameters extends CloudParameters {
    // begin constants
    public static final int MAX_NUMBER_MESSAGES = 1;
    public static final int NUM_RETRIES = 7;
    public static final int START_SECONDS_TO_RETRY = 1;
    public static final String NO_SUCH_KEY = "NoSuchKey";
    // end constants

    // begin instance variables
    private AWSCredentials credentials;
    private AmazonS3 s3;
    public AmazonSQS sqs;
    public int visibility;
    // end instance variables

    public AWSParameters() throws ParameterException {
	super();
	credentials = makeCredentials();
	s3 = makeS3();
	sqs = makeSQS();
	visibility = visibility();
    }
	
    public String getInputBucket() {
	return param( INPUT_BUCKET_NAME_ID );
    }

    public String getOutputBucket() {
	return param( OUTPUT_BUCKET_NAME_ID );
    }

    public String getQueueURL() {
	return param( QUEUE_URL_ID );
    }

    public String getEnvironmentBucket() {
	return param( ENVIRONMENT_BUCKET_NAME_ID );
    }

    public String getEnvironmentPrefix() {
	return param( ENVIRONMENT_PREFIX_ID );
    }
    
    public String getAnalysisProgramName() {
	return param( ANALYSIS_PROGRAM_NAME_ID );
    }

    public String getEnvironmentZip() {
	return param( ENVIRONMENT_ZIP_FILE_ID );
    }

    /**
     * Makes a credentials object from these params
     */
    protected AWSCredentials makeCredentials() {
	return new BasicAWSCredentials( param( ACCESS_KEY_ID ),
					param( SECRET_KEY_ID ) );
    }

    /**
     * Gets the credentials object.
     * If one has already been made, it will return that one.
     */
    public AWSCredentials getCredentials() {
	return credentials;
    }

    /**
     * Makes an S3 handle for this
     */
    protected AmazonS3 makeS3() {
	return new AmazonS3Client( getCredentials() );
    }

    public AmazonS3 getS3() {
	return s3;
    }

    protected AmazonSQS makeSQS() {
	return new AmazonSQSClient( getCredentials() );
    }
    
    public AmazonSQS getSQS() {
	return sqs;
    }

    /**
     * Makes an SQS message request with the given visibility and
     * max number of messages
     */
    public ReceiveMessageRequest makeMessageRequest( int visibility,
						     int maxNumber ) {
	return new ReceiveMessageRequest( getQueueURL() )
	    .withVisibilityTimeout( new Integer( visibility ) )
	    .withMaxNumberOfMessages( new Integer( maxNumber ) );
    }

    /**
     * Makes a message request using <code>VISIBILITY</code>
     * and <code>MAX_NUMBER_MESSAGES</code>
     */
    public ReceiveMessageRequest makeMessageRequest() {
	return makeMessageRequest( visibility,
				   MAX_NUMBER_MESSAGES );
    }

    /**
     * Gets messages from SQS using the given request
     */
    public List< Message > getMessages( ReceiveMessageRequest request ) {
	return getSQS().receiveMessage( request ).getMessages();
    }

    /**
     * Gets messages from SQS using the request from 
     * <code>makeMessageRequest()</code>
     */
    public List< Message > getMessages() {
	return getMessages( makeMessageRequest() );
    }

    /**
     * Makes a request to delete the given message
     */
    public DeleteMessageRequest makeDeleteMessageRequest( Message message ) {
	return new DeleteMessageRequest( getQueueURL(),
					 message.getReceiptHandle() );
    }

    public void deleteMessage( DeleteMessageRequest request ) {
	getSQS().deleteMessage( request );
    }

    public void deleteMessage( Message message ) {
	deleteMessage( makeDeleteMessageRequest( message ) );
    }

    /**
     * Gets the given file from the given bucket
     */
    public void getObjectNoRetry( String bucket,
				  String fileName,
				  File localFile ) {
	getS3().getObject( new GetObjectRequest( bucket,
						 fileName ),
			   localFile );
    }

    /**
     * This will do some retries.
     * Due to eventual consistency, this sometimes fails to get an object
     * that should be possible to get.
     */
    public void getObject( String bucket,
			   String fileName,
			   File localFile ) {
	int seconds = START_SECONDS_TO_RETRY;
	for( int x = 0; x < NUM_RETRIES; x++ ) {
	    try {
		getObjectNoRetry( bucket, fileName, localFile );
		break;
	    } catch ( AmazonServiceException e ) {
		if ( e.getErrorCode().equals( NO_SUCH_KEY ) ) { //HACK
		    try {
			Thread.sleep( seconds );
		    } catch ( InterruptedException e1 ) {
			throw e;
		    }
		    seconds *= 2;
		} else {
		    throw e;
		}
	    }
	}
    }

    /**
     * Gets the given file from the input bucket
     */
    public void getObject( String fileName,
			   File localFile ) {
	getObject( getInputBucket(),
		   fileName,
		   localFile );
    }

    public void putObject( String bucket,
			   String fileName,
			   File localFile ) {
	getS3().putObject( bucket,
			   fileName,
			   localFile );
    }

    /**
     * Puts the given file in the output bucket
     */
    public void putObject( String fileName,
			   File localFile ) {
	putObject( getOutputBucket(),
		   fileName,
		   localFile );
    }

    /**
     * Executes the given string in the execution environment
     */
    public String executeProgramInEnvironment( String args ) 
	throws IOException {
	String toExecute = 
	    "cd '" + getEnvironmentPrefix() + "'; " + args;
	return executeProgram( new String[]{ "sh", "-c",
					     toExecute } );
    }

    /**
     * Analyzes a file with the given name in the execution environment
     */
    public String doAnalysis( String fileName ) 
	throws IOException {
	String executionString =
	    "./" + getAnalysisProgramName() + " '" + fileName + "'";
	return executeProgramInEnvironment( executionString );
    }

    public void prepEnvironment() throws IOException {
	Download.downloadPrefix( getS3(),
				 getEnvironmentBucket(),
				 getEnvironmentZip() );
	executeProgram( new String[]{ "unzip", 
				      getEnvironmentZip() } );
	makeExecutableInDir( getEnvironmentPrefix() );
    }

    /**
     * Returns the output of the program in a single string.
     */
    public static String executeProgram( String[] args ) 
	throws IOException {
	String retval = "";
	String line;
	Process process = 
	    Runtime.getRuntime().exec( args );
	InputStream inputStream = process.getInputStream();
	BufferedReader reader =
	    new BufferedReader( new InputStreamReader( inputStream ) );
	while ( ( line = reader.readLine() ) != null ) {
	    retval += line;
	}
	reader.close();
	inputStream.close();
	process.getOutputStream().close();
	process.getErrorStream().close();
	return retval;
    }

    /**
     * Makes the file encoded by the given path executable.
     * Refers to the local disk.
     */
    public static void makeExecutable( String fileName ) throws IOException {
	executeProgram( new String[]{ "chmod",
				      "a+x",
				      fileName } );
    }

    /**
     * Makes all files in the given directory path executable
     * Refers to the local disk.
     */
    public static void makeExecutableInDir( String dirName ) throws IOException {
	for( File current : new File( dirName ).listFiles() ) {
	    makeExecutable( current.getPath() );
	}
    }

    public static AWSParameters makeInitialAWSParameters() 
	throws ParameterException, IOException {
	AWSParameters retval = new AWSParameters();
	retval.prepEnvironment();
	return retval;
    }
} // AWSParameters 
