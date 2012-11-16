import java.io.*;

/**
 * Starts up instances on AWS.
 * @author Kyle Dewey
 */
public class StartInstances {
    public static void usage() {
	System.err.println( "Needs the following params:\n" +
			    "-The number of instances to start\n" +
			    "-The price per instance hour\n" +
			    "-A filename containing parameters" );
    }

    public static void main( String[] args ) {
	if ( args.length != 3 ) {
	    usage();
	    System.exit( 1 );
	}

	try {
	    int numInstances = Integer.parseInt( args[ 0 ] );
	    InstanceStarterParameters.makeParameters( args[ 2 ] )
		.requestInstances( numInstances, 
				   args[ 1 ] );
	} catch ( NumberFormatException e ) {
	    System.err.println( "The number of instances must be a positive integer" );
	} catch ( IllegalArgumentException e ) {
	    System.err.println( e.getMessage() );
	} catch ( ParameterException e ) {
	    System.err.println( e.getMessage() );
	} catch ( IOException e ) {
	    e.printStackTrace();
	    System.err.println( e );
	}
    }
}
