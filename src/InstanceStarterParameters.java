import com.amazonaws.*;
import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.sqs.*;
import com.amazonaws.services.sqs.model.*;

import java.io.*;
import java.util.*;

/**
 * Parameters needed for starting instances.
 * @author Kyle Dewey
 */
public class InstanceStarterParameters extends AWSParameters {
    // begin constants
    // needed params
    public static final String KEY_PAIR_ID = "keyPair";
    public static final String IMAGE_ID_ID = "imageID";
    public static final String SECURITY_GROUP_ID = "securityGroup";
    public static final String INSTANCE_TYPE_ID = "instanceType";
    
    // optional params
    public static final String SHUTDOWN_BEHAVIOR_ID = "shutdownBehavior";
    public static final String DEFAULT_SHUTDOWN_BEHAVIOR = "terminate";

    public static final String SPOT_TYPE_ID = "spotType";
    public static final String DEFAULT_SPOT_TYPE = "one-time";

    private static final Set< String > NEEDED_PARAMS =
	new HashSet< String >() {
	{
	    add( KEY_PAIR_ID );
	    add( IMAGE_ID_ID );
	    add( SECURITY_GROUP_ID );
	    add( INSTANCE_TYPE_ID );
	}
    };

    private static final Map< String, String > OPTIONAL_PARAMS =
	new HashMap< String, String >() {
	{
	    put( SHUTDOWN_BEHAVIOR_ID,
		 DEFAULT_SHUTDOWN_BEHAVIOR );
	    put( SPOT_TYPE_ID,
		 DEFAULT_SPOT_TYPE );
	}
    };
    // end constants

    // begin instance variables
    private final InstanceType instanceType;
    // end instance variables

    public InstanceStarterParameters( Map< String, String > input ) 
	throws ParameterException {
	super( input );
	instanceType = makeInstanceType();
	validateAWSParams();
    }

    // TODO - figure out a way to correct this terrible code duplication
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
    
    public String getKeyPair() {
	return param( KEY_PAIR_ID );
    }

    public String getImageId() {
	return param( IMAGE_ID_ID );
    }

    public String getSecurityGroup() {
	return param( SECURITY_GROUP_ID );
    }

    public InstanceType getInstanceType() {
	return instanceType;
    }

    public String getShutdownBehavior() {
	return param( SHUTDOWN_BEHAVIOR_ID );
    }

    public String getSpotType() {
	return param( SPOT_TYPE_ID );
    }

    public static void validateSpotType( String spotType )
	throws ParameterException {
	validateInSet( spotType,
		       new String[]{ "one-time", "persistent" },
		       "The spot type must either be \"one-time\"" +
		       " or \"persistent\"" );
    }

    public static void validateShutdownBehavior( String stored )
	throws ParameterException {
	validateInSet( stored,
		       new String[]{ "terminate", "stop" },
		       "The shutdown behavior must be either " +
		       "\"terminate\" or \"stop\"" );
    }

    protected InstanceType makeInstanceType() {
	return InstanceType.valueOf( param( INSTANCE_TYPE_ID ) );
    }

    public static void validateInstanceType( String stored ) 
	throws ParameterException {
	try {
	    InstanceType.valueOf( stored );
	} catch ( IllegalArgumentException e ) {
	    throw new ParameterException( "Unknown instance type: " + stored );
	}
    }

    /**
     * To be called after <code>params</code> is set
     */
    protected void validateS3Params() throws ParameterException {
	if ( !doesInputBucketExist() ) {
	    throw new ParameterException( "Input bucket does not exist: \"" +
					  getInputBucket() + "\"" );
	}
	if ( !doesOutputBucketExist() ) {
	    throw new ParameterException( "Output bucket does not exist: \"" +
					  getOutputBucket() + "\"" );
	}
	if ( !doesEnvironmentBucketExist() ) {
	    throw new ParameterException( "Environment bucket does not exist: \"" +
					  getEnvironmentBucket() + "\"" );
	}
	if ( !doesEnvironmentZipExist() ) {
	    throw new ParameterException( "Environment bucket does not contain " +
					  "the environment zip file: \"" +
					  getEnvironmentZip() + "\"" );
	}
    }

    /**
     * To be called after <code>params</code> is set
     */
    protected void validateSQSParams() throws ParameterException {
	if ( !doesQueueExist() ) {
	    throw new ParameterException( "SQS queue does not exist with URL: \"" +
					  getQueueUrl() + "\"" );
	}
    }

    /**
     * To be called after <code>params</code> is set
     */
    protected void validateAWSParams() throws ParameterException {
	validateS3Params();
	validateSQSParams();
    }
	     
    /**
     * Overridden to validate the shutdown behavior, the instance type,
     * and the spot type.
     */
    protected Map< String, String > makeParams( Map< String, String > input )
	throws ParameterException {
	Map< String, String > retval = super.makeParams( input );
	validateShutdownBehavior( retval.get( SHUTDOWN_BEHAVIOR_ID ) );
	validateInstanceType( retval.get( INSTANCE_TYPE_ID ) );
	validateSpotType( retval.get( SPOT_TYPE_ID ) );
	return retval;
    }

    public void validateNumInstances( int numInstances ) {
	if ( numInstances <= 0 ) {
	    throw new IllegalArgumentException( "The number of instances " +
						"must be a " +
						"positive integer" );
	} 
	int approx = approximateNumEnqueuedMessages();
	if ( approx < numInstances ) {
	    throw new IllegalArgumentException( "Attempted to request more " +
						"instances than there are " +
						"files to process; estimated " +
						approx + " files left" );
	}
    }

    public static void validatePrice( String price ) {
	try {
	    double d = Double.parseDouble( price );
	    if ( d <= 0.0 ) {
		throw new NumberFormatException();
	    }
	} catch ( NumberFormatException e ) {
	    throw new IllegalArgumentException( "The price must be a " +
						"positive real number" );
	}
    }

    public String makeUserData() {
	try {
	    return Base64.encodeBytes( new AWSParameters( getAllParameters() )
				       .toMinimalString()
				       .getBytes() );
	} catch ( ParameterException e ) {
	    // impossible
	    e.printStackTrace();
	    System.err.println( e );
	    System.exit( 1 );
	}
	return null; // impossible
    }

    public LaunchSpecification makeLaunchSpecification() {
	return new LaunchSpecification()
	    .withImageId( getImageId() )
	    .withInstanceType( getInstanceType() )
	    .withKeyName( getKeyPair() )
	    .withSecurityGroups( getSecurityGroup() )
	    .withUserData( makeUserData() );
    }

    /**
     * Generates the request for the given number of instances
     * @throws IllegalArgumentException If the number of instances and the
     * set max price is not positive
     */
    public RequestSpotInstancesRequest makeRequest( int numInstances, String price ) {
	validateNumInstances( numInstances );
	validatePrice( price );
	return new RequestSpotInstancesRequest()
	    .withInstanceCount( new Integer( numInstances ) )
	    .withLaunchSpecification( makeLaunchSpecification() )
	    .withSpotPrice( price )
	    .withType( getSpotType() );
    }

    public RequestSpotInstancesResult requestInstances( int numInstances, 
							String price ) {
	return getEC2().requestSpotInstances( 
		 makeRequest( numInstances, price ) );
    }

    public static InstanceStarterParameters makeParameters() 
	throws IOException, ParameterException {
	return new InstanceStarterParameters( Parameters.readMapFromFile() );
    }

    public static InstanceStarterParameters makeParameters( String filename ) 
	throws IOException, ParameterException {
	return new InstanceStarterParameters( Parameters.readMapFromFile( filename ) );
    }
}