import com.amazonaws.*;
import com.amazonaws.auth.*;
import com.amazonaws.services.sqs.*;
import com.amazonaws.services.sqs.model.*;

import java.util.*;
import java.util.Map.Entry;
import java.io.*;

public class ListQueue {
    public static final Integer VISIBILITY = new Integer( 10 );
    public static final Integer MAX_NUM = new Integer( 10 );

    /**
     * Flattens the given list of lists
     */
    public static < T > List< T > flatten( List< List< T > > lists ) {
	List< T > retval = new ArrayList< T >();
	for( List< T > current : lists ) {
	    retval.addAll( current );
	}
	return retval;
    }

    /**
     * Lists all messages with the queue with the given URL
     * Note that it sets the timeout to 10 seconds so that we can get all
     * messages.
     */
    public static List< Message > getMessages( AmazonSQS sqs, 
					       String url ) throws IOException {
	List< List< Message > > temp = new LinkedList< List< Message > >();
	List< Message > currentAdded = null;

	do {
	    currentAdded = sqs.receiveMessage( new ReceiveMessageRequest( url )
					       .withVisibilityTimeout( VISIBILITY )
					       .withMaxNumberOfMessages( MAX_NUM ) ).getMessages();
	    temp.add( currentAdded );
	} while ( currentAdded.size() > 0 );

	return flatten( temp );
    }

    public static void printMessage( Message message ) {
	System.out.println("  Message");
	System.out.println("    MessageId:     " + message.getMessageId());
	System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
	System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
	System.out.println("    Body:          " + message.getBody());
	for (Entry<String, String> entry : message.getAttributes().entrySet()) {
	    System.out.println("  Attribute");
	    System.out.println("    Name:  " + entry.getKey());
	    System.out.println("    Value: " + entry.getValue());
	}
    }

    public static void printMessages( List< Message > messages ) {
	for( Message message : messages ) {
	    printMessage( message );
	}
    }

    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Needs a queue name" );
	    System.exit( 1 );
	}

	try {
	    AmazonSQS sqs = new AmazonSQSClient( LocalParameters.makeParameters().makeCredentials() );
	    printMessages( getMessages( sqs, 
					BucketToQueue.makeQueue( sqs, args[ 0 ]  ) ) );
	} catch ( Exception e ) {
	    e.printStackTrace();
	    System.err.println( e );
	}
    }
}
