import com.amazonaws.*;
import com.amazonaws.auth.*;
import com.amazonaws.services.sqs.*;
import com.amazonaws.services.sqs.model.*;

import java.io.*;

public class DeleteQueue {
    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Needs the name of a queue." );
	    System.exit( 1 );
	}
	
	try {
	    AmazonSQS sqs = new AmazonSQSClient( LocalParameters.makeParameters().makeCredentials() );
	    sqs.deleteQueue( new DeleteQueueRequest( BucketToQueue.makeQueue( sqs, args[ 0 ]  ) ) );
	} catch ( Exception e ) {
	    e.printStackTrace();
	    System.err.println( e );
	}
    }
}
