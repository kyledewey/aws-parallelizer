import java.net.*;
import java.io.*;
import java.util.*;

public class HTTPGet {
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

    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Needs a URL to connect to." );
	    System.exit( 1 );
	}
	
	try {
	    for( String current : readFromURL( args[ 0 ] ) ) {
		System.out.println( current );
	    }
	} catch ( MalformedURLException e ) {
	    System.err.println( e );
	} catch ( ProtocolException e ) {
	    System.err.println( e );
	} catch ( IOException e ) {
	    System.err.println( e );
	}
    }
}
