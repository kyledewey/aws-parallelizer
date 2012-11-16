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

public class MakeBucket {
    public static File[] makeFilesFromArgs( String[] args ) {
	File[] retval = new File[ args.length - 1 ];
	for( int x = 1; x < args.length; x++ ) {
	    retval[ x - 1 ] = new File( args[ x ] );
	}
	return retval;
    }

    public static void main( String[] args ) {
	if ( args.length < 2 ) {
	    System.err.println( "Needs a name of the bucket and the files to put in it." );
	    System.exit( 1 );
	}
	try {
	    AmazonS3 s3 = CredentialParameters.makeParameters().getS3();
	    s3.createBucket( args[ 0 ] );
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
