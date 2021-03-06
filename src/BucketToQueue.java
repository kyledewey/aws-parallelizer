import com.amazonaws.*;
import com.amazonaws.auth.*;
import com.amazonaws.services.sqs.*;
import com.amazonaws.services.sqs.model.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;

import java.util.*;
import java.io.*;

/**
 * Given an AWS bucket, it will put all the filenames found in that
 * bucket into an SQS queue.
 * @author Kyle Dewey
 */
public class BucketToQueue {
    // begin instance variables
    private final AWSParameters params;
    private final MakeQueue makeQueue;
    // end instance variables

    public BucketToQueue( AWSParameters params ) {
	this.params = params;
	this.makeQueue = new MakeQueue( params );
    }

    /**
     * @return the URL of the created queue
     */
    public String bucketToQueue( String bucketName,
				 String queueName ) throws IOException {
	String queueURL = makeQueue.makeQueue( queueName );
	for( String fileName : ListBucket.listBucket( params.getS3(), bucketName ) ) {
	    params.getSQS().sendMessage( new SendMessageRequest( queueURL, fileName ) );
	}
	return queueURL;
    }

    public static void main( String[] args ) {
	if ( args.length != 2 ) {
	    System.err.println( "Needs the name of an S3 bucket and a name of an SQS queue." );
	    System.exit( 1 );
	}

	try {
	    BucketToQueue btq = new BucketToQueue( AWSParameters.makeLocalParameters() );
	    System.out.println( "Queue URL: " + 
				btq.bucketToQueue( args[ 0 ], args[ 1 ] ) );
	} catch ( Exception e ) {
	    e.printStackTrace();
	    System.err.println( e );
	}
    }
}
