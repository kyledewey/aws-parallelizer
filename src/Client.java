import java.io.*;
import java.net.*;

/**
 * Runs on AWS.
 * @author Kyle Dewey
 */
public class Client {
    // begin constants
    public static final String SHOULD_SHUTDOWN_SENTINEL =
	"/tmp/should_shutdown";
    public static final String[] SHOULD_SHUTDOWN_COMMAND =
	new String[]{ "touch",
		      SHOULD_SHUTDOWN_SENTINEL };
    // end constants

    // begin instance variables
    private final AWSParameters parameters;
    // end instance variables

    public Client() 
	throws MalformedURLException, ProtocolException, IOException, ParameterException {
	parameters = AWSParameters.makeParameters();
	makeShutdownSentinel();
	parameters.prepEnvironment();
    }

    public Worker makeWorker() throws IOException {
	int numThreads = parameters.getNumThreads();
	if ( numThreads == 1 ) {
	    return new SequentialWorker( parameters );
	} else {
	    return new SMPWorker( parameters, numThreads );
	}
    }

    protected void makeShutdownSentinel() throws IOException {
	if ( parameters.getShouldShutdown() ) {
	    JobControl.executeProgram( SHOULD_SHUTDOWN_COMMAND );
	}
    }

    public static void main( String[] args ) {
	try {
	    new Client().makeWorker().processFiles();
	} catch ( Exception e ) {
	    e.printStackTrace();
	    System.err.println( e );
	}
    }
}
