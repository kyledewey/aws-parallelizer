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
	    .getSpotInstanceRequests();
    }

    public List<String> getOpenActiveSpotInstanceRequestIds() {
	return spotInstanceRequestIds(getOpenActiveSpotInstanceRequests());
    }

    public static List<String> spotInstanceRequestIds(List<SpotInstanceRequest> requests) {
	List<String> retval = new ArrayList<String>();
	for (SpotInstanceRequest request : requests) {
	    retval.add(request.getSpotInstanceRequestId());
	}
	return retval;
    }

    public DescribeInstancesRequest makeInstancesRequest() {
	return new DescribeInstancesRequest()
	    .withFilters( Arrays.asList( new Filter( "instance-state-name",
						     Arrays.asList( "pending", "running" ) ) ) );
    }

    public CancelSpotInstanceRequestsResult cancelSpotInstanceRequests() {
	List<String> ids = getOpenActiveSpotInstanceRequestIds();
	if (!ids.isEmpty()) {
	    CancelSpotInstanceRequestsRequest request = new CancelSpotInstanceRequestsRequest(ids);
	    return params.getEC2().cancelSpotInstanceRequests(request);
	} else {
	    return null;
	}
    }

    public List<Instance> getPendingRunningInstances() {
	List<Instance> retval = new ArrayList<Instance>();
	List<Reservation> reservations = params.getEC2()
	    .describeInstances( makeInstancesRequest() )
	    .getReservations();
	
	for ( Reservation reservation : reservations ) {
	    retval.addAll( reservation.getInstances() );
	}

	return retval;
    }

    public List<String> getPendingRunningInstanceIds() {
	List<Instance> instances = getPendingRunningInstances();
	return instanceIds(instances);
    }

    public static List<String> instanceIds(List<Instance> instances) {
	List<String> retval = new ArrayList<String>();
	for(Instance instance : instances) {
	    retval.add(instance.getInstanceId());
	}
	return retval;
    }

    public TerminateInstancesResult terminateInstances() {
	List<String> ids = getPendingRunningInstanceIds();
	if (!ids.isEmpty()) {
	    TerminateInstancesRequest request = new TerminateInstancesRequest(ids);
	    return params.getEC2().terminateInstances(request);
	} else {
	    return null;
	}
    }

    public void panicButton() {
	cancelSpotInstanceRequests();
	terminateInstances();
    }

    public static void main(String[] args) {
	try {
	    new KillInstances(AWSParameters.makeLocalParameters()).panicButton();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.err.println(e);
	}
    }
} // KillInstances

