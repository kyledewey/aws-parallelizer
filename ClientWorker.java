import com.amazonaws.*;
import com.amazonaws.auth.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.sqs.*;
import com.amazonaws.services.sqs.model.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientWorker {
    // begin instance variables
    public AWSParameters parameters;
    // end instance variables
    
    /**
     * Creates a worker that uses the given parameters.
     */
    public ClientWorker( AWSParameters parameters ) throws IOException {
	this.parameters = parameters;
	parameters.prepEnvironment();
    }

    /**
     * Automatically gets all parameters
     */
    public ClientWorker() throws ParameterException, IOException {
	this( new AWSParameters() );
    }

    /**
     * Gets the next file to process.
     * If there are none, this returns null.
     */
    public Message nextFile() {
	Message retval = null;
	List< Message > messages = parameters.getMessages();
	if ( messages.size() > 0 ) {
	    retval = messages.get( 0 );
	}

	return retval;
    }

    /**
     * Deletes the message in the queue that we have a file.
     */
    public void doneWithFile( Message message ) {
	parameters.deleteMessage( message );
    }

    public void processFile( String fileName ) throws IOException {
	parameters.getObject( fileName,
			      new File( parameters.getEnvironmentPrefix(),
					fileName ) );
	String outputFileName = parameters.doAnalysis( fileName );
	if ( !outputFileName.equals( "" ) ) {
	    File outputFile = new File( parameters.getEnvironmentPrefix(),
					outputFileName );
	    if ( !outputFile.exists() ) {
		outputFile = new File( outputFileName );
	    }
	    
	    if ( outputFile.exists() ) {
		parameters.putObject( outputFile.getName(),
				      outputFile );
		outputFile.delete();
	    }
	}
    }

    public void processFiles() throws IOException {
	Message nextFile;

	while( ( nextFile = nextFile() ) != null ) {
	    new VisibilityTimeoutRunnable( this, nextFile ).run();
	}
    }

    public static void main( String[] args ) {
	try {
	    new ClientWorker().processFiles();
	} catch ( ParameterException e ) {
	    e.printStackTrace();
	    System.err.println( e );
	} catch ( IOException e ) {
	    e.printStackTrace();
	    System.err.println( e );
	}
    }
}
