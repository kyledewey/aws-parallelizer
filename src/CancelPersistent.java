/**
 * Class that will cancel this instance's persistent request, if
 * it was a persistent request.  There is no harm in calling
 * this on a non-persistent request.
 * @author Kyle Dewey
 */
public class CancelPersistent {
    public static void main( String[] args ) {
	try {
	    CredentialParameters
		.makeParametersOnAWS()
		.cancelMeIfPersistentSpot();
	} catch ( Exception e ) {
	    e.printStackTrace();
	    System.err.println( e );
	}
    }
}
