import java.io.*;
import java.util.*;

import com.amazonaws.services.sqs.model.*;

public abstract class Worker {
    // begin constants
    public static String FILE_SEPARATOR = ":";
    // end constants
    
    // begin instance variables
    private final AWSParameters parameters;
    // end instance variables

    public Worker( AWSParameters parameters ) {
	this.parameters = parameters;
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

    protected void tryUploadFile(String fileName) throws IOException {
        if ( !fileName.equals( "" ) ) {
	    File outputFile = new File( parameters.getEnvironmentPrefix(),
					fileName );
	    if ( !outputFile.exists() ) {
		outputFile = new File( fileName );
	    }
	    
	    if ( outputFile.exists() ) {
		parameters.putObject( outputFile.getName(),
				      outputFile );
		outputFile.delete();
	    }
        }
    }

    public void processFile( String fileName ) throws IOException {
	File inputFile = new File( parameters.getEnvironmentPrefix(),
				   fileName );
	parameters.getObject( fileName, inputFile );
        String output = parameters.doAnalysis( fileName );
        for ( String outputFileName : output.split( FILE_SEPARATOR ) ) {
            tryUploadFile( outputFileName );
        }
	inputFile.delete();
    }

    public AWSParameters getParameters() {
	return parameters;
    }

    public abstract void processFiles();
}
