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
    // begin constants
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int MINUTES_PER_HOUR = 60;
    public static final int HOURS_PER_DAY = 24;
    public static final int MAX_NUM_DAYS_SQS_ALLOWS = 14;
    public static final int MAX_RETENTION_PERIOD =
	SECONDS_PER_MINUTE * MINUTES_PER_HOUR * HOURS_PER_DAY * MAX_NUM_DAYS_SQS_ALLOWS;
    // end constants

    // begin instance variables
    private final AWSParameters params;
    // end instance variables

    public BucketToQueue( AWSParameters params ) {
	this.params = params;
    }

    public CreateQueueRequest makeQueueRequest( String queueName ) {
	Map< String, String > attrib = new HashMap< String, String >();
	attrib.put( QueueAttributeName.VisibilityTimeout.name(),
		    Integer.toString( params.getVisibilityTimeout() ) );
	attrib.put( QueueAttributeName.MessageRetentionPeriod.name(),
		    Integer.toString( MAX_RETENTION_PERIOD ) );
	return new CreateQueueRequest( queueName ).withAttributes( attrib );
    }

    /**
     * Creates a queue with the given object.
     * If the queue already exists, it gets the URL of the existing queue.
     */
    public String makeQueue( String queueName ) throws IOException {
	return params.getSQS().createQueue( makeQueueRequest( queueName ) ).getQueueUrl();
    }

    /**
     * @return the URL of the created queue
     */
    public String bucketToQueue( String bucketName,
				 String queueName ) throws IOException {
	String queueURL = makeQueue( queueName );
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
