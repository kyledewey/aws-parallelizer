/*
 * QueueClient.java
 *
 * Version:
 *     $Id: QueueClient.java,v 1.1 2012/11/06 01:12:28 kyle Exp $
 *
 * Revisions:
 *      $Log: QueueClient.java,v $
 *      Revision 1.1  2012/11/06 01:12:28  kyle
 *      Initial revision
 *
 *
 */

import java.util.*;
import java.net.*;
import java.io.*;

public class QueueClient {
    public static void sendHello( Socket socket ) throws IOException {
	QueueServer.writeMessage( socket,
				  QueueServer.CLIENT_HELLO );
    }

    /**
     * Gets the next message from the given server on the given
     * port
     */
    public static String nextMessage( String serverName,
				      int port ) 
	throws UnknownHostException, IOException {
	Socket socket = new Socket( serverName, port );
	sendHello( socket );
	String retval = QueueServer.getMessage( socket );
	socket.close();
	return retval;
    }
    
    public static void main( String[] args ) {
	if ( args.length != 2 ) {
	    System.err.println( "Need a server name and a port number." );
	}
	try {
	    System.out.println( nextMessage( args[ 0 ],
					     Integer.parseInt( args[ 1 ] ) ) );
	} catch ( NumberFormatException e ) {
	    System.err.println( e );
	} catch ( UnknownHostException e ) {
	    System.err.println( e );
	} catch ( IOException e ) {
	    System.err.println( e );
	}
    }
}
