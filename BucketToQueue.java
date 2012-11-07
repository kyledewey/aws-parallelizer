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
    /**
     * Creates a queue with the given object.
     * If the queue already exists, it gets the URL of the existing queue.
     */
    public static String makeQueue( AmazonSQS sqs, 
				    String queueName ) throws IOException {
	return sqs.createQueue( new CreateQueueRequest( queueName ) )
	    .getQueueUrl();
    }

    /**
     * @return the URL of the created queue
     */
    public static String bucketToQueue( AmazonS3 s3,
					AmazonSQS sqs,
					String bucketName,
					String queueName ) throws IOException {
	String queueURL = makeQueue( sqs, queueName );
	for( String fileName : ListBucket.listBucket( s3, bucketName ) ) {
	    sqs.sendMessage( new SendMessageRequest( queueURL, fileName ) );
	}
	return queueURL;
    }

    public static void main( String[] args ) {
	if ( args.length != 2 ) {
	    System.err.println( "Needs the name of an S3 bucket and a name of an SQS queue." );
	    System.exit( 1 );
	}

	try {
	    AWSCredentials credentials = 
		LocalParameters.makeParameters().makeCredentials();
	    AmazonS3 s3 = new AmazonS3Client( credentials );
	    AmazonSQS sqs = new AmazonSQSClient( credentials );
	    System.out.println( "Queue URL: " + bucketToQueue( s3,
							       sqs,
							       args[ 0 ],
							       args[ 1 ] ) );
	} catch ( Exception e ) {
	    e.printStackTrace();
	    System.err.println( e );
	}
    }
}
