import com.amazonaws.*;
import com.amazonaws.auth.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.sqs.*;
import com.amazonaws.services.sqs.model.*;

import java.io.*;
import java.net.*;
import java.util.*;


/**
 * Parameters that are needed on AWS.
 * @author Kyle Dewey
 */
public class AWSParameters extends CredentialParameters {
    // begin constants
    // begin constants for parameters
    // needed parameters
    public static final String INPUT_BUCKET_NAME_ID = "inputBucket";
    public static final String OUTPUT_BUCKET_NAME_ID = "outputBucket";
    public static final String QUEUE_URL_ID = "queueURL";
    public static final String ENVIRONMENT_BUCKET_NAME_ID = "environmentBucket";
    public static final String ENVIRONMENT_PREFIX_ID = "environmentPrefix";
    public static final String ENVIRONMENT_ZIP_FILE_ID = "environmentZip";
    public static final String ANALYSIS_PROGRAM_NAME_ID = "analysisProgram";

    // optional parameters
    // visibility timeout, in seconds
    public static final String VISIBILITY_TIMEOUT_ID = "visibilityTimeout";
    public static final int DEFAULT_VISIBILITY_TIMEOUT = 60 * 10; // 10 minutes

    // number of threads to use
    // 0 means use the max available
    public static final String NUM_THREADS_ID = "numThreads";
    public static final int DEFAULT_NUM_THREADS = 0;

    // whether or not to shutdown on termination
    public static final String SHOULD_SHUTDOWN_ID = "shouldShutdown";
    public static final boolean DEFAULT_SHOULD_SHUTDOWN = true;

    private static final Set< String > NEEDED_PARAMS = 
	new HashSet< String >() { 
	{
	    add( INPUT_BUCKET_NAME_ID );
	    add( OUTPUT_BUCKET_NAME_ID );
	    add( QUEUE_URL_ID );
	    add( ENVIRONMENT_BUCKET_NAME_ID );
	    add( ENVIRONMENT_PREFIX_ID );
	    add( ENVIRONMENT_ZIP_FILE_ID );
	    add( ANALYSIS_PROGRAM_NAME_ID );
	}
    };

    private static final Map< String, String > OPTIONAL_PARAMS =
	new HashMap< String, String >() {
	{
	    put( VISIBILITY_TIMEOUT_ID,
		 Integer.toString( DEFAULT_VISIBILITY_TIMEOUT ) );
	    put( NUM_THREADS_ID,
		 Integer.toString( DEFAULT_NUM_THREADS ) );
	    put( SHOULD_SHUTDOWN_ID,
		 Boolean.toString( DEFAULT_SHOULD_SHUTDOWN ) );
	}
    };
    // end constants for parameters

    // begin constants for processing
    public static final int MAX_NUMBER_MESSAGES = 1;
    public static final int NUM_RETRIES = 7;
    public static final int START_SECONDS_TO_RETRY = 1;
    public static final String NO_SUCH_KEY = "NoSuchKey";

    // for interfacing with SQS
    public static final String APPROXIMATE_NUM_MESSAGES = 
	"ApproximateNumberOfMessages";
    // end constants for processing
    // end constants

    // begin instance variables
    private final int numThreads;
    private final int visibilityTimeout;
    private final boolean shouldShutdown;
    // end instance variables

    public AWSParameters( Map< String, String > input ) throws ParameterException {
	super( input );
	numThreads = makeNumThreads();
	visibilityTimeout = makeVisibilityTimeout();
	shouldShutdown = makeShouldShutdown();
    }

    public Map< String, String > getOptionalParams() {
	Map< String, String > retval = 
	    new HashMap< String, String >( super.getOptionalParams() );
	retval.putAll( OPTIONAL_PARAMS );
	return retval;
    }

    public Set< String > getNeededParams() {
	Set< String > retval = new HashSet< String >( super.getNeededParams() );
	retval.addAll( NEEDED_PARAMS );
	return retval;
    }

    public String getInputBucket() {
	return param( INPUT_BUCKET_NAME_ID );
    }

    public String getOutputBucket() {
	return param( OUTPUT_BUCKET_NAME_ID );
    }

    public String getQueueUrl() {
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

    protected int makeVisibilityTimeout() {
	int retval = DEFAULT_VISIBILITY_TIMEOUT;
	try {
	    retval = Integer.parseInt( param( VISIBILITY_TIMEOUT_ID ) );
	} catch ( NumberFormatException e ) {
	    // shouldn't be possible
	    e.printStackTrace();
	    System.err.println( e );
	}
	return retval;
    }

    public int getVisibilityTimeout() {
	return visibilityTimeout;
    }

    protected boolean makeShouldShutdown() {
	return Boolean.parseBoolean( param( SHOULD_SHUTDOWN_ID ) );
    }

    public boolean getShouldShutdown() {
	return shouldShutdown;
    }

    protected int makeNumThreads() {
	int retval = Runtime.getRuntime().availableProcessors();
	try {
	    int vis = Integer.parseInt( param( NUM_THREADS_ID ) );
	    if ( vis > 0 ) { // < 0 should be impossible
		retval = vis;
	    }
	} catch ( NumberFormatException e ) {
	    // shouldn't be possible
	    e.printStackTrace();
	    System.err.println( e );
	}
	return retval;
    }

    public int getNumThreads() {
	return numThreads;
    }

    public static void validateNonNegative( String stored,
					    String message ) throws ParameterException {
	try {
	    int num = Integer.parseInt( stored );
	    if ( num < 0 ) {
		throw new NumberFormatException();
	    }
	} catch ( NumberFormatException e ) {
	    throw new ParameterException( message );
	}
    }

    public static void validateInSet( String stored,
				      String[] valid,
				      String message ) throws ParameterException {
	validateInSet( stored, 
		       new HashSet< String >( Arrays.asList( valid ) ),
		       message );
    }

    public static void validateInSet( String stored,
				      Set< String > valid,
				      String message ) throws ParameterException {
	if ( !valid.contains( stored ) ) {
	    throw new ParameterException( message );
	}
    }

    public static void validateBoolean( String stored,
					String message ) throws ParameterException {
	validateInSet( stored,
		       new String[]{ "true", "false" },
		       message );
    }

    public static void validateShouldShutdown( String stored ) throws ParameterException {
	validateBoolean( stored,
			 "Whether or not to shutdown must be either \"true\"" +
			 " or \"false\"" );
    }

    public static void validateNumThreads( String stored ) throws ParameterException {
	validateNonNegative( stored,
			     "The number of threads must be a non-negative integer" );
    }

    public static void validateVisibility( String stored ) throws ParameterException {
	validateNonNegative( stored,
			     "The visibility timeout must be a non-negative integer" );
    }

    /**
     * Overridden to validate the visibility timout and the number of threads.
     */
    protected Map< String, String > makeParams( Map< String, String > input ) 
	throws ParameterException {
	Map< String, String > retval = super.makeParams( input );

	validateNumThreads( retval.get( NUM_THREADS_ID ) );
	validateVisibility( retval.get( VISIBILITY_TIMEOUT_ID ) );
	validateShouldShutdown( retval.get( SHOULD_SHUTDOWN_ID ) );

	return retval;
    }

    /**
     * Makes an SQS message request with the given visibility and
     * max number of messages
     */
    public ReceiveMessageRequest makeMessageRequest( int visibility,
						     int maxNumber ) {
	return new ReceiveMessageRequest( getQueueUrl() )
	    .withVisibilityTimeout( new Integer( visibility ) )
	    .withMaxNumberOfMessages( new Integer( maxNumber ) );
    }

    /**
     * Makes a message request using <code>VISIBILITY</code>
     * and <code>MAX_NUMBER_MESSAGES</code>
     */
    public ReceiveMessageRequest makeMessageRequest() {
	return makeMessageRequest( getVisibilityTimeout(),
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
	return new DeleteMessageRequest( getQueueUrl(),
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
	return JobControl.executeProgram( new String[]{ "sh", "-c",
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
	Download.download( getS3(),
			   getEnvironmentBucket(),
			   getEnvironmentZip() );
	JobControl.executeProgram( new String[]{ "unzip", 
						 getEnvironmentZip() } );
	JobControl.makeExecutableInDir( getEnvironmentPrefix() );
    }

    public GetQueueAttributesRequest approximateNumEnqueuedMessagesRequest() {
	return new GetQueueAttributesRequest( getQueueUrl() )
	    .withAttributeNames( APPROXIMATE_NUM_MESSAGES );
    }
    
    public int approximateNumEnqueuedMessages() {
	try {
	    return Integer.parseInt( 
		     getSQS().getQueueAttributes( 
		       approximateNumEnqueuedMessagesRequest() )
		     .getAttributes().get( APPROXIMATE_NUM_MESSAGES ) );
	} catch ( NumberFormatException e ) {
	    // impossible
	    e.printStackTrace();
	    System.err.println( e );
	}
	throw new AmazonServiceException( "Possible non-existent SQS " +
					  "queue or massive API error" );
    }

    public boolean doesInputBucketExist() {
	return doesBucketExist( getInputBucket() );
    }

    public boolean doesOutputBucketExist() {
	return doesBucketExist( getOutputBucket() );
    }

    public boolean doesEnvironmentBucketExist() {
	return doesBucketExist( getEnvironmentBucket() );
    }

    public boolean doesEnvironmentZipExist() {
	return doesObjectExistInBucket( getEnvironmentZip(),
					getEnvironmentBucket() );
    }

    /**
     * Checks that the overall run queue exists.
     */
    public boolean doesQueueExist() {
	return doesQueueExist( getQueueUrl() );
    }

    public static AWSParameters makeParameters() 
	throws IOException, ParameterException {
	return new AWSParameters( InstanceHelpers.readMapFromURL() );
    }

    public static AWSParameters makeLocalParameters()
	throws IOException, ParameterException {
	return new AWSParameters( Parameters.readMapFromFile() );
    }
}
