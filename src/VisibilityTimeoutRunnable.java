import com.amazonaws.services.sqs.model.*;

import java.io.IOException;

public class VisibilityTimeoutRunnable {
    // begin constants
    public static final int MILLISECONDS_IN_MINUTE = 1000 * 60;
    // end constants

    // begin instance variables
    private final Worker worker;
    private final AWSParameters parameters;
    private final Message message;
    private final Thread timer;
    private boolean done;
    // end instance variables

    public VisibilityTimeoutRunnable( Worker worker, Message message ) {
	this.worker = worker;
	this.message = message;
	parameters = worker.getParameters();
	timer = 
	    new Thread( new Runnable() {
		    public void run() {
			int baseTimeout = parameters.getVisibilityTimeout();
			boolean shouldRun = true;
			while( shouldRun && !Thread.currentThread().interrupted() ) {
			    try {
				Thread.sleep( baseTimeout * 1000 - MILLISECONDS_IN_MINUTE );
				updateVisibilityTimeout();
			    } catch ( InterruptedException e ) {
				shouldRun = false;
			    }
			}
		    }
		} );
    }

    public void updateVisibilityTimeout() {
	parameters.getSQS().changeMessageVisibility( 
	  new ChangeMessageVisibilityRequest( parameters.param( AWSParameters.QUEUE_URL_ID ), 
					      message.getReceiptHandle(),
					      parameters.getVisibilityTimeout() ) );
    }

    public void run() {
	boolean errorOccurred = false;
	timer.start();
	try {
	    worker.processFile( message.getBody() );
	} catch ( IOException e ) {
	    // try to go to the next one
	    // don't mark as complete though; it could be a transient problem
	    errorOccurred = true;
	} finally {
	    timer.interrupt();
	}

	try {
	    timer.join();
	    if ( !errorOccurred ) {
		worker.doneWithFile( message );
	    }
	} catch ( InterruptedException e ) {
	    // the timer didn't join in time
	    // give up on it
	}
    }
}
