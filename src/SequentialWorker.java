import com.amazonaws.services.sqs.model.*;

import java.io.*;

public class SequentialWorker extends Worker {
    /**
     * Creates a worker that uses the given parameters.
     */
    public SequentialWorker( AWSParameters parameters ) throws IOException {
	super( parameters );
    }

    public void processFiles() throws IOException {
	Message nextFile;

	while( ( nextFile = nextFile() ) != null ) {
	    new VisibilityTimeoutRunnable( this, nextFile ).run();
	}
    }
}
