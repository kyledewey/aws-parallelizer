import java.io.*;

/**
 * Starts up instances on AWS.
 * @author Kyle Dewey
 */
public class StartInstances {
    public static void main( String[] args ) {
	if ( args.length != 2 ) {
	    System.err.println( "Needs a number of instances and the max price per instance" );
	    System.exit( 1 );
	}

	try {
	    int numInstances = Integer.parseInt( args[ 0 ] );
	    InstanceStarterParameters.makeParameters().requestInstances( numInstances, 
									 args[ 1 ] );
	} catch ( NumberFormatException e ) {
	    System.err.println( "The number of instances must be a positive integer" );
	} catch ( IllegalArgumentException e ) {
	    System.err.println( e.getMessage() );
	} catch ( ParameterException e ) {
	    e.printStackTrace();
	    System.err.println( e );
	} catch ( IOException e ) {
	    e.printStackTrace();
	    System.err.println( e );
	}
    }
}
