import com.amazonaws.*;
import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;

import java.util.*;
import java.io.*;

/**
 * Cancels any open or active spot instance requests, and terminates any instances
 * that are running
 * @author Kyle Dewey
 */
public class KillInstances {
    // begin instance variables
    private final AWSParameters params;
    // end instance variables

    public KillInstances( AWSParameters params ) {
	this.params = params;
    }

    public DescribeSpotInstanceRequestsRequest makeSpotRequestRequest() {
	return new DescribeSpotInstanceRequestsRequest()
	    .withFilters( Arrays.asList( new Filter( "state",
						     Arrays.asList( "active", "open" ) ) ) );
    }

    public List<SpotInstanceRequest> getOpenActiveSpotInstanceRequests() {
	return params.getEC2()
	    .describeSpotInstanceRequests( makeSpotRequestRequest() )
	    getSpotInstanceRequests();
    }

    public DescribeInstancesRequest makeInstancesRequest() {
	return new DescribeInstancesRequest()
	    .withFilters( Arrays.asList( new Filter( "instance-state-name",
						     Arrays.asList( "pending", "running" ) ) ) );
    }

    public void cancelSpotInstanceRequests() {
	params.getEC2()
	    .cancelSpotInstanceRequests( // STOPPED HERE
    public List<Instance> getPendingRunningInstances() {
	List<Instance> retval = new ArrayList<Instance>()
	List<Reservation> reservations = params.getEC2()
	    .describeInstances( makeInstancesRequest() )
	    .getReservations();
	
	for ( Reservation reservation : reservations ) {
	    retval.addAll( reservation.getInstances() );
	}

	return retval;
    }
