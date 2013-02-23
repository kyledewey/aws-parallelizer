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

public class PrepBucket {
    public static File[] makeFilesFromArgs( String[] args ) {
	File[] retval = new File[ args.length - 1 ];
	for( int x = 1; x < args.length; x++ ) {
	    retval[ x - 1 ] = new File( args[ x ] );
	}
	return retval;
    }

    public static void main( String[] args ) {
	if ( args.length < 1 ) {
	    System.err.println( "Usage: java PrepBucket bucket_name [file0 file 1 ... fileN]" );
	    System.exit( 1 );
	}
	try {
	    CredentialParameters params = CredentialParameters.makeParameters();
	    AmazonS3 s3 = params.getS3();
	    if ( !params.doesBucketExist( args[ 0 ] ) ) {
		s3.createBucket( args[ 0 ] );
	    }
	    for( File current : makeFilesFromArgs( args ) ) {
		s3.putObject( args[ 0 ], 
			      current.getName(),
			      current );
	    }
	} catch( Exception e ) {
	    e.printStackTrace();
	    System.err.println( e );
	}
    }
}
