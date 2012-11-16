import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Various helper routines that only work on AWS but are not associated
 * with any particular credentials
 * @author Kyle Dewey
 */
public class InstanceHelpers {
    // begin constants
    // where user data is located on the cloud
    public static final String USER_DATA_LOCATION =
	"http://169.254.169.254/latest/user-data";
    public static final String INSTANCE_ID_LOCATION =
	"http://169.254.169.254/latest/meta-data/instance-id";
    // end constants

    /**
     * Off of AWS this will not work.
     */
    public static String getInstanceId() throws IOException {
	HttpURLConnection connection = 
	    NetHelpers.connectionFromURL( INSTANCE_ID_LOCATION );
	try {
	    return new BufferedReader( 
			 new InputStreamReader( 
			       connection.getInputStream() ) ).readLine();
	} finally {
	    connection.disconnect();
	}
    }
    
    public static Map< String, String > readMapFromURL()
	throws IOException, ParameterException {
	return NetHelpers.readMapFromURL( USER_DATA_LOCATION );
    }
}