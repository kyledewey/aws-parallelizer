import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Helper routines associated with network activity
 * @author Kyle Dewey
 */
public class NetHelpers {
    public static HttpURLConnection connectionFromURL( String url )
	throws  IOException {
	List< String > retval = new LinkedList< String >();
	HttpURLConnection connection = 
	    (HttpURLConnection)( new URL( url ).openConnection() );
	connection.setRequestMethod( "GET" );
	connection.setDoOutput( true );
	connection.connect();
	return connection;
    }

    public static Map< String, String > readMapFromURL( String url )
	throws IOException, ParameterException {
	HttpURLConnection connection = connectionFromURL( url );
	try {
	    BufferedReader reader =
		new BufferedReader( 
		      new InputStreamReader( connection.getInputStream() ) );
	    return Parameters.readMapFromBufferedReader( reader );
	} finally {
	    connection.disconnect();
	}
    }
}