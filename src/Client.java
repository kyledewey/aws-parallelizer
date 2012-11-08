import java.io.*;

public class Client {
    public static Worker makeWorker( AWSParameters parameters ) throws IOException {
	int numThreads = parameters.numThreads();
	if ( numThreads == 1 ) {
	    return new SequentialWorker( parameters );
	} else {
	    return new SMPWorker( parameters, numThreads );
	}
    }

    public static void main( String[] args ) {
	try {
	    makeWorker( AWSParameters.makeInitialAWSParameters() ).processFiles();
	} catch ( ParameterException e ) {
	    e.printStackTrace();
	    System.err.println( e );
	} catch ( IOException e ) {
	    e.printStackTrace();
	    System.err.println( e );
	}
    }
}
