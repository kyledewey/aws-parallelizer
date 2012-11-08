import java.io.*;
import java.util.concurrent.*;

public class SMPWorker extends Worker {
    // begin instance variables
    private final int numThreads;
    private final ExecutorService executor;
   // end instance variables

    public SMPWorker( AWSParameters parameters ) throws IOException {
	this( parameters,
	      parameters.numThreads() );
    }

    public SMPWorker( AWSParameters parameters,
		      int numThreads ) throws IOException {
	super( parameters );
	this.numThreads = numThreads;
	executor = Executors.newFixedThreadPool( numThreads );
    }

    protected Runnable makeWorker() {
	return new Runnable() {
	    public void run() {
		try {
		    new SequentialWorker( parameters ).processFiles();
		} catch ( IOException e ) {
		    e.printStackTrace();
		    System.err.println( e.toString() );
		    System.err.println( "Failed to make sequential worker." );
		}
	    }
	};
    }

    public void processFiles() throws IOException {
	for( int x = 0; x < numThreads; x++ ) {
	    executor.submit( makeWorker() );
	}
	try {
	    executor.shutdown();
	    executor.awaitTermination( Long.MAX_VALUE,
				       TimeUnit.SECONDS );
	} catch ( InterruptedException e ) {}
    }
}
