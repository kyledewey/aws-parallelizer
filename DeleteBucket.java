import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.UUID;
import java.util.*;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class DeleteBucket {
    public static List< String > keys = new LinkedList< String >();
    public static void getObjects( ObjectListing listing ) throws IOException {
	for( S3ObjectSummary summary : listing.getObjectSummaries() ) {
	    keys.add( summary.getKey() );
	}
    }

    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Needs the name of a bucket to delete." );
	    System.exit( 1 );
	}

	try {
	    AmazonS3 s3 = new AmazonS3Client( LocalParameters.makeParameters().makeCredentials() );
	    
	    ObjectListing listing = 
		s3.listObjects( new ListObjectsRequest().withBucketName( args[ 0 ] ) );
	    
	    do {
		getObjects( listing );
		listing = s3.listNextBatchOfObjects( listing );
	    } while( listing.isTruncated() );
	    getObjects( listing );
	    for( String key : keys ) {
		s3.deleteObject( args[ 0 ], key );
	    }
	    s3.deleteBucket( args[ 0 ] );
	} catch ( Exception e ) {
	    e.printStackTrace();
	    System.err.println( e );
	}
    }
}
