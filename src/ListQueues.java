import com.amazonaws.*;
import com.amazonaws.auth.*;
import com.amazonaws.services.sqs.*;
import com.amazonaws.services.sqs.model.*;

public class ListQueues {
    public static void main( String[] args ) {
	try {
	    AmazonSQS sqs = CredentialParameters.makeParameters().getSQS();
	    for( String url : sqs.listQueues().getQueueUrls() ) {
		System.out.println( url );
	    }
	} catch ( Exception e ) {
	    e.printStackTrace();
	    System.err.println( e );
	}
    }
}
