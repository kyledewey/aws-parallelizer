/*
 * Parameters.java
 *
 * Version:
 *     $Id: Parameters.java,v 1.1 2012/11/06 01:12:28 kyle Exp $
 *
 * Revisions:
 *      $Log: Parameters.java,v $
 *      Revision 1.1  2012/11/06 01:12:28  kyle
 *      Initial revision
 *
 *
 */

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Abstracts away parameters for ClientWorker.
 * @author Kyle Dewey
 */
public abstract class Parameters {
    // begin constants
    // delimiter for keys and values
    public static final char KEY_VALUE_DELIM = '=';

    // delimeter for making error messages
    public static final String ERROR_DELIM = ", ";

    // the names of needed parameters
    public static final String INPUT_BUCKET_NAME_ID = "inputBucket";
    public static final String OUTPUT_BUCKET_NAME_ID = "outputBucket";
    public static final String QUEUE_URL_ID = "queueURL";
    public static final String ENVIRONMENT_BUCKET_NAME_ID = "environmentBucket";
    public static final String ENVIRONMENT_PREFIX_ID = "environmentPrefix";
    public static final String ANALYSIS_PROGRAM_NAME_ID = "analysisProgram";
    public static final String ACCESS_KEY_ID = "accessKey";
    public static final String SECRET_KEY_ID = "secretKey";
    public static final String KEY_PAIR_ID = "keyPair";
    public static final Set< String > NEEDED_PARAMS =
	new HashSet< String >() {
	{
	    add( INPUT_BUCKET_NAME_ID );
	    add( OUTPUT_BUCKET_NAME_ID );
	    add( QUEUE_URL_ID );
	    add( ENVIRONMENT_BUCKET_NAME_ID );
	    add( ENVIRONMENT_PREFIX_ID );
	    add( ANALYSIS_PROGRAM_NAME_ID );
	    add( ACCESS_KEY_ID );
	    add( SECRET_KEY_ID );
	    add( KEY_PAIR_ID );
	}
    };

    // the names of optional parameters, along with their default values
    // the number of nodes to run
    public static final String NUM_NODES_ID = "numNodes";
    public static final int DEFAULT_NUM_NODES = 1;

    // the image ID to use
    public static final String IMAGE_ID_ID = "imageID";
    public static final String DEFAULT_IMAGE_ID = "ami-8253aceb"; // autorun4

    // the security group to use
    public static final String SECURITY_GROUP_ID = "securityGroup";
    public static final String DEFAULT_SECURITY_GROUP = "quick-start-1";

    // the shutdown behavior
    public static final String SHUTDOWN_BEHAVIOR_ID = "shutdownBehavior";
    public static final String DEFAULT_SHUTDOWN_BEHAVIOR = "terminate";

    // the type of instance to spawn
    public static final String INSTANCE_TYPE_ID = "instanceType";
    public static final String DEFAULT_INSTANCE_TYPE = "t1.micro";

    // visibility timeout time, in seconds
    public static final String VISIBILITY_TIMEOUT_ID = "visibilityTimeout";
    public static final int DEFAULT_VISIBILITY_TIMEOUT = 60 * 10; // 10 minutes

    public static final Map< String, String > OPTIONAL_PARAMS =
	new HashMap< String, String >() {
	{
	    put( NUM_NODES_ID, 
		 Integer.toString( DEFAULT_NUM_NODES ) );
	    put( IMAGE_ID_ID, 
		 DEFAULT_IMAGE_ID );
	    put( SECURITY_GROUP_ID, 
		 DEFAULT_SECURITY_GROUP );
	    put( SHUTDOWN_BEHAVIOR_ID, 
		 DEFAULT_SHUTDOWN_BEHAVIOR );
	    put( INSTANCE_TYPE_ID, 
		 DEFAULT_INSTANCE_TYPE );
	    put( VISIBILITY_TIMEOUT_ID,
		 Integer.toString( DEFAULT_VISIBILITY_TIMEOUT ) ); 
	}
    };
    // end constants

    // begin instance variables
    protected Map< String, String > parameters;
    // end instance variables

    /**
     * Loads in all parameters.
     * @throws ParameterException If parameters were invalid for whatever reason
     */
    public Parameters() throws ParameterException {
	parameters = getParameters();
	validateParams();
	addOptionalParams( parameters );
    }

    /**
     * Validates that the given name is that of a parameter
     */
    public void validateParamName( String name ) throws ParameterException {
	if ( !parameters.containsKey( name ) ) {
	    throw new ParameterException( "Unrecognized parameter name: " + name );
	}
    }

    /**
     * Gets the parameter value by the given name.
     * @throws ParameterException If the name is unrecognized.
     */
    public String getParam( String name ) throws ParameterException {
	validateParamName( name );
	return parameters.get( name );
    }

    /**
     * Gets the param with the given name.
     * Returns null if the parameter doesn't exist
     */
    public String param( String name ) {
	String retval = null;
	try {
	    retval = getParam( name );
	} catch( ParameterException e ) {}
	return retval;
    }

    /**
     * Parses a key/value pair.
     * @param data The data to parse
     * @return The key and the value in a pair as the first and the
     * second items, respectively.  Returns null if there isn't
     * a key/value pair on this line
     */
    public static Pair< String, String > parseKeyValuePair( String data ) {
	Pair< String, String > retval = null;
	int firstDelim = data.indexOf( KEY_VALUE_DELIM );

	if ( firstDelim >= 0 ) {
	    retval = new Pair< String, String >( data.substring( 0, firstDelim ),
						 data.substring( firstDelim + 1 ) );
	}
	return retval;
    }

    /**
     * Parses in the given input data.
     * It is expected that there is one key/value pair per line,
     * in the format key=value
     * @param lines All the lines in the given file
     * @return All the key value pairs.
     */
    public static Map< String, String > parseInitLines( String[] lines ) {
	Map< String, String > retval = new HashMap< String, String >();
	for( String line : lines ) {
	    Pair< String, String > pair = parseKeyValuePair( line );
	    if ( pair != null ) {
		retval.put( pair.first,
			    pair.second );
	    }
	}
	return retval;
    }

    /**
     * Gets raw parameter input from an input source of some kind
     * @return an array of strings, one per line
     * @throws IOException If an error occurred on reading in the parameters
     */
    public abstract String[] readRawParameters() throws IOException;

    /**
     * Gets the parameters for this instance.
     * @throws ParameterException If a needed parameter is missing
     */
    public Map< String, String > getParameters() 
	throws ParameterException {
	try {
	    return parseInitLines( readRawParameters() );
	} catch ( MalformedURLException e ) {
	    throw new ParameterException( e.getMessage() );
	} catch ( ProtocolException e ) {
	    throw new ParameterException( e.getMessage() );
	} catch ( IOException e ) {
	    throw new ParameterException( e.getMessage() );
	}
    }

    /**
     * Makes a parameter line
     * @param key The key of the parameter
     * @param value The value of the parameter
     * @return key + KEY_VALUE_DELIM + value
     */
    public static String paramLine( String key,
				    String value ) {
	return key + KEY_VALUE_DELIM + value;
    }

    /**
     * Makes a parameter line with the given param
     */
    public String paramLine( String key ) throws ParameterException {
	return paramLine( key,
			  getParam( key ) );
    }

    /**
     * Gets all the keys which are not recognized in the given map.
     */
    public static List< String > unknownKeys( Map< String, String > params ) {
	List< String > retval = new ArrayList< String >();
	for( String key : params.keySet() ) {
	    if ( !NEEDED_PARAMS.contains( key ) &&
		 !OPTIONAL_PARAMS.containsKey( key ) ) {
		retval.add( key );
	    }
	}
	Collections.sort( retval );
	return retval;
    }

    /**
     * gets the required keys that are missing in the given map.
     */
    public static List< String > missingNeededKeys( Map< String, String > params ) {
	List< String > retval = new ArrayList< String >();
	for( String key : NEEDED_PARAMS ) {
	    if ( !params.containsKey( key ) ) {
		retval.add( key );
	    }
	}
	Collections.sort( retval );
	return retval;
    }

    public static String join( List< String > items, String delim ) {
	String retval = "";
	for( String item : items ) {
	    retval += item + delim;
	}
	return retval.substring( 0, retval.length() - delim.length() );
    }

    public static String join( List< String > items ) {
	return join( items, ERROR_DELIM );
    }

    /**
     * Like <code>validateParams</code>, but it uses the state
     */
    public void validateParams() throws ParameterException {
	validateParams( parameters );
    }

    /**
     * Validates the given parameters.
     * This checks that all keys are recognized, and that we
     * have all neccessary parameters.
     * @param params The parameters to check
     * @throws ParameterException If we don't recognize all keys, or if
     * we are missing a needed parameter
     */
    public static void validateParams( Map< String, String > params )
	throws ParameterException {
	List< String > unknown = unknownKeys( params );
	if ( unknown.size() > 0 ) {
	    throw new ParameterException( "Unrecognized parameters: " +
					  join( unknown ) );
	}
	List< String > missing = missingNeededKeys( params );
	if ( missing.size() > 0 ) {
	    throw new ParameterException( "Missing needed parameters: " +
					  join( missing ) );
	}
    }

    /**
     * Adds optional parameters to the given map.
     * That is, for each optional parameter not already in the map,
     * it will add the optional parameter along with its default value.
     * Note that this is destructive
     * @pre The parameters are valid, as determined by <code>validateParams()</code>
     */
    public static void addOptionalParams( Map< String, String > params ) {
	for( String key : OPTIONAL_PARAMS.keySet() ) {
	    if ( !params.containsKey( key ) ) {
		params.put( key, 
			    OPTIONAL_PARAMS.get( key ) );
	    }
	}
    }
} // Parameters

