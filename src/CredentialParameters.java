import com.amazonaws.*;
import com.amazonaws.auth.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.sqs.*;
import com.amazonaws.services.sqs.model.*;
import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;

import java.io.*;
import java.util.*;

/**
 * Parameters needed to work with credentials.
 * @author Kyle Deewey
 */
public class CredentialParameters extends Parameters {
    // begin constants
    // needed parameters
    public static final String ACCESS_KEY_ID = "accessKey";
    public static final String SECRET_KEY_ID = "secretKey";

    // optional parameters
    // the region to use for both EC2 and SQS
    public static final String EC2_SQS_REGION_ID = "EC2SQSRegion";
    public static final String DEFAULT_EC2_SQS_REGION = "us-east-1";
    public static final String REGION_PREFIX = ".";
    public static final String REGION_SUFFIX = ".amazonaws.com";

    private static final Map< String, String > OPTIONAL_PARAMS =
	new HashMap< String, String >() {
	{
	    put( EC2_SQS_REGION_ID,
		 DEFAULT_EC2_SQS_REGION );
	}
    };

    private static final Set< String > NEEDED_PARAMS =
	new HashSet< String >() {
	{
 	    add( ACCESS_KEY_ID );
	    add( SECRET_KEY_ID );
	}
    };
    // end constants

    // begin instance variables
    private final AWSCredentials credentials;
    private final AmazonS3 s3;
    private final AmazonSQS sqs;
    private final AmazonEC2 ec2;
    // end instance variables

    public CredentialParameters( Map< String, String > input ) throws ParameterException {
	super( input );
	credentials = makeCredentials();
	s3 = makeS3();
	sqs = makeSQS();
	ec2 = makeEC2();
    }

    public Set< String > getNeededParams() {
	return NEEDED_PARAMS;
    }

    public Map< String, String > getOptionalParams() {
	return OPTIONAL_PARAMS;
    }

    private AWSCredentials makeCredentials() {
	return new BasicAWSCredentials( param( ACCESS_KEY_ID ),
					param( SECRET_KEY_ID ) );
    }

    public AWSCredentials getCredentials() {
	return credentials;
    }

    public String getSQSRegion() {
	return toSQSFullRegion( param( EC2_SQS_REGION_ID ) );
    }

    public String getEC2Region() {
	return toEC2FullRegion( param( EC2_SQS_REGION_ID ) );
    }

    private AmazonEC2 makeEC2() {
	AmazonEC2Client retval = new AmazonEC2Client( getCredentials() );
	retval.setEndpoint( getEC2Region() );
	return retval;
    }

    public AmazonEC2 getEC2() {
	return ec2;
    }

    private AmazonS3 makeS3() {
	return new AmazonS3Client( getCredentials() );
    }

    public AmazonS3 getS3() {
	return s3;
    }

    private AmazonSQS makeSQS() {
	AmazonSQSClient retval =  new AmazonSQSClient( getCredentials() );
	retval.setEndpoint( getSQSRegion() );
	return retval;
    }
    
    public AmazonSQS getSQS() {
	return sqs;
    }

    public boolean doesBucketExist( String bucket ) {
	return getS3().doesBucketExist( bucket );
    }

    public boolean doesObjectExistInBucket( String objectName, String bucket ) {
	try {
	    getS3().getObjectMetadata( bucket, objectName );
	    return true;
	} catch ( AmazonS3Exception e ) {
	    if ( e.getStatusCode() == 404 ) {
		return false;
	    } else {
		throw e;
	    }
	}
    }

    public List< String > listQueuesByUrl() {
	return getSQS().listQueues().getQueueUrls();
    }

    /**
     * @param url The queue URL
     */
    public boolean doesQueueExist( String url ) {
	// TODO: there must be a more efficient way to do this
	return listQueuesByUrl().contains( url );
    }

    protected List< com.amazonaws.services.ec2.model.Filter > persistentSpotRequestFilters() 
	throws IOException {
	return Arrays.asList( new com.amazonaws.services.ec2.model.Filter( "instance-id",
                                                                           Arrays.asList( InstanceHelpers.getInstanceId() ) ),
			      new com.amazonaws.services.ec2.model.Filter( "type",
                                                                           Arrays.asList( "persistent" ) ) );
    }

    protected DescribeSpotInstanceRequestsRequest makeSpotRequestRequest() 
	throws IOException {
	return new DescribeSpotInstanceRequestsRequest().withFilters( 
		     persistentSpotRequestFilters() );
    }

    /**
     * Gets the persistent spot instance request id corresponding to this instance,
     * or null if the instance isn't backed by a persistent spot instance request.
     */
    public String getSpotInstanceRequestId() 
	throws IOException {
	List< SpotInstanceRequest > requests =
	    getEC2()
	    .describeSpotInstanceRequests( makeSpotRequestRequest() )
	    .getSpotInstanceRequests();
	if ( requests.size() == 0 ) {
	    // not a spot request
	    return null;
	} else if ( requests.size() == 1 ) {
	    return requests.get( 0 ).getSpotInstanceRequestId();
	} else {
	    // should be impossible based on filter conditions
	    throw new AmazonClientException( "Got more than one match for spot instance " +
					     "request" );
	}
    }
	    
    /**
     * Cancels the spot request with the given spot instance request id
     */
    public CancelSpotInstanceRequestsResult cancelSpotRequest( String spotInstanceRequestId ) 
	throws IOException {
	return getEC2()
	    .cancelSpotInstanceRequests( 
	       new CancelSpotInstanceRequestsRequest( 
		    Arrays.asList( spotInstanceRequestId ) ) );
    }

    /**
     * Cancels the spot request backing this instance, but only if this
     * instance is a persistent spot request.
     * Returns null if it's not.
     */
    public CancelSpotInstanceRequestsResult cancelMeIfPersistentSpot() 
	throws IOException {
	String id = getSpotInstanceRequestId();
	return ( id != null ) ? cancelSpotRequest( id ) : null;
    }

    public static String toSQSFullRegion( String shortRegion ) {
	return toFullRegion( "sqs", shortRegion );
    }

    public static String toEC2FullRegion( String shortRegion ) {
	return toFullRegion( "ec2", shortRegion );
    }

    /**
     * Given a short region like "us-east-1", it will return a full
     * region like "sqs.us-east-1.amazonaws.com".
     */
    public static String toFullRegion( String serviceName,
				       String shortRegion ) {
	return serviceName + REGION_PREFIX + shortRegion + REGION_SUFFIX;
    }

    public static CredentialParameters makeParameters()
	throws IOException, ParameterException {
	return new CredentialParameters( Parameters.readMapFromFile() );
    }

    public static CredentialParameters makeParametersOnAWS()
	throws IOException, ParameterException {
	return new CredentialParameters( InstanceHelpers.readMapFromURL() );
    }
}
	    
