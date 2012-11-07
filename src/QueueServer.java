/*
 * QueueServer.java
 *
 * Version:
 *     $Id: QueueServer.java,v 1.1 2012/11/06 01:12:28 kyle Exp $
 *
 * Revisions:
 *      $Log: QueueServer.java,v $
 *      Revision 1.1  2012/11/06 01:12:28  kyle
 *      Initial revision
 *
 *
 */

import java.util.*;
import java.net.*;
import java.io.*;

/**
 * A server that holds a queue.
 * Each request pops off from the queue.
 * @author Kyle Dewey
 */
public class QueueServer {
    // begin constants
    public static final int DEFAULT_PORT = 8675;
    public static final int DEFAULT_BACKLOG = 1000;
    public static final String DEFAULT_QUEUE_EMPTY = "/////queue empty/////";
    public static final String CLIENT_HELLO = "QueueClient";
    // end constants

    // begin instance variables
    private Queue< String > queue; // holds the items
    private String queueEmpty; // message to send when the queue is empty
    private ServerSocket server; // socket to make connections with
    // end instance variables

    /**
     * Creates a server that listens on the given port and has the
     * given backlog size.  It holds the given items and it 
     */
    public QueueServer( Collection< String > items,
			String queueEmpty,
			int port,
			int backlog ) throws IOException {
	server = new ServerSocket( port, backlog );
	queue = new LinkedList< String >( items );
    }
	
    public QueueServer( Collection< String > items,
			String queueEmpty ) throws IOException {
	this( items,
	      queueEmpty,
	      DEFAULT_PORT,
	      DEFAULT_BACKLOG );
    }

    public QueueServer( Collection< String > items ) throws IOException {
	this( items,
	      DEFAULT_QUEUE_EMPTY );
    }

    /**
     * Gets the message from the given client.
     * Note that it is constrained to a single line.
     */
    public static String getMessage( Socket client ) throws IOException {
	return new BufferedReader( new InputStreamReader( client.getInputStream() ) ).readLine();
    }

    /**
     * Gets the next item from the queue.
     * If the queue is empty, then it returns queueEmpty
     */
    public String nextItem() {
	String retval = queue.poll();
	if ( retval == null ) {
	    retval = queueEmpty;
	}
	return retval;
    }

    /**
     * Writes the given string to the given client socket
     * Note that it assumes it is all one one line.
     */
    public static void writeMessage( Socket client,
				     String message ) throws IOException {
	PrintWriter writer = new PrintWriter( client.getOutputStream() );
	writer.print( message + "\n" );
	writer.flush();
    }

    /**
     * Processes a single client request.
     */
    public void accept() throws IOException {
	Socket client = server.accept();
	
	if ( getMessage( client ).equals( CLIENT_HELLO ) ) {
	    writeMessage( client, nextItem() );
	}
	client.close();
    }

    public static void main( String[] args ) {
	try {
	    QueueServer server = new QueueServer( Arrays.asList( args ) );
	    while( true ) {
		server.accept();
	    }
	} catch ( IOException e ) {
	    System.err.println( e );
	}
    }
}

