/*
 * CloudParameters.java
 *
 * Version:
 *     $Id: CloudParameters.java,v 1.1 2012/11/06 01:12:28 kyle Exp $
 *
 * Revisions:
 *      $Log: CloudParameters.java,v $
 *      Revision 1.1  2012/11/06 01:12:28  kyle
 *      Initial revision
 *
 *
 */

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Parses in parameters from the user data of this instance.
 * Assumes that this is running on an instance.
 * @author Kyle Dewey
 */
public class CloudParameters extends Parameters {
    // begin constants
    // where user data is located
    public static final String USER_DATA_LOCATION =
	"http://169.254.169.254/latest/user-data";
    // end constants

    /**
     * Creates a new CloudParameters object
     * @throws ParameterException If a parameter was incorrect
     */
    public CloudParameters() throws ParameterException {
	super();
    }

    /**
     * Gets the raw parameters from the URL
     * @throws IOException If an error occurred on reading in the information
     */
    public String[] readRawParameters() throws IOException {
	try {
	    return readFromURL( USER_DATA_LOCATION );
	} catch ( MalformedURLException e ) {
	    e.printStackTrace();
	    throw new IOException( e.toString() );
	} catch ( ProtocolException e ) {
	    e.printStackTrace();
	    throw new IOException( e.toString() );
	}
    }

    /**
     * Reads information from an HTTP URL.
     * Returns each line as an item in an array.
     * @param url The HTTP URL to read from
     * @return All the lines from the given page accessable via HTTP, one line per index.
     */
    public static String[] readFromURL( String url ) 
	throws MalformedURLException, ProtocolException, IOException {
	List< String > retval = new LinkedList< String >();
	HttpURLConnection connection = 
	    (HttpURLConnection)( new URL( url ).openConnection() );
	connection.setRequestMethod( "GET" );
	connection.setDoOutput( true );
	connection.connect();
	BufferedReader reader =
	    new BufferedReader( new InputStreamReader( connection.getInputStream() ) );
	String line = null;

	while ( ( line = reader.readLine() ) != null ) {
	    retval.add( line );
	}
	connection.disconnect();
	return retval.toArray( new String[ 0 ] );
    }
}
