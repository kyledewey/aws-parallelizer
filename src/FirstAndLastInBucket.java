import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

import com.amazonaws.*;
import com.amazonaws.auth.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;

public class FirstAndLastInBucket {

    public static List< Date > listObjects( ObjectListing listing ) 
	throws IOException {
	List< Date > retval = new ArrayList< Date >();
	for( S3ObjectSummary summary : listing.getObjectSummaries() ) {
	    retval.add( summary.getLastModified() );
	}
	return retval;
    }

    public static List< Date > listBucket( AmazonS3 s3,
					   String bucketName,
					   String prefix ) 
	throws IOException {
	List< Date > retval = new ArrayList< Date >();
	ObjectListing listing = s3.listObjects( bucketName, 
						prefix );
	do {
	    retval.addAll( listObjects( listing ) );
	    listing = s3.listNextBatchOfObjects( listing );
	} while( listing.isTruncated() );
	retval.addAll( listObjects( listing ) );
	return retval;
    }

    public static List< Date > listBucket( AmazonS3 s3,
					   String bucketName )
	throws IOException {
	return listBucket( s3,
			   bucketName,
			   "" );
    }

    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Needs the name of a bucket to ls on." );
	    System.exit( 1 );
	}

	try {
	    AmazonS3 s3 = new AmazonS3Client( LocalParameters.makeParameters().makeCredentials() );
	    List< Date > lastModified = listBucket( s3, args[ 0 ] );
	    Collections.sort( lastModified );
	    if ( lastModified.size() == 0 ) {
		System.out.println( "Bucket is empty" );
	    } else {
		System.out.println( "Start output: " + lastModified.get( 0 ) );
		System.out.println( "End output: " + lastModified.get( lastModified.size() - 1 ) );
	    }
	} catch ( Exception e ) {
	    e.printStackTrace();
	    System.err.println( e );
	}
    }
}
