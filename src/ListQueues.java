import com.amazonaws.*;
import com.amazonaws.auth.*;
import com.amazonaws.services.sqs.*;
import com.amazonaws.services.sqs.model.*;

public class ListQueues {
    public static void main( String[] args ) {
	try {
	    CredentialParameters params = CredentialParameters.makeParameters();
	    for( String url : params.listQueuesByUrl() ) {
		System.out.println( url );
	    }
	} catch ( Exception e ) {
	    e.printStackTrace();
	    System.err.println( e );
	}
    }
}
