import com.amazonaws.*;
import com.amazonaws.auth.*;
import com.amazonaws.services.sqs.*;
import com.amazonaws.services.sqs.model.*;

import java.util.*;
import java.io.*;

/**
 * Makes an SQS queue, if it does not already exist.
 * @author Kyle Dewey
 */
public class MakeQueue {
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

    public MakeQueue( AWSParameters params ) {
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

    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Usage: java MakeQueue queue_name" );
	    System.exit( 1 );
	}

	try {
	    MakeQueue maker = new MakeQueue( AWSParameters.makeLocalParameters() );
	    System.out.println( "Queue URL: " + maker.makeQueue( args[ 0 ] ) );
	} catch ( Exception e ) {
	    e.printStackTrace();
	    System.err.println( e );
	}
    }
}

