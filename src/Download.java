import com.amazonaws.*;
import com.amazonaws.auth.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;

import java.io.*;

/**
 * Downloads all the files with the given S3 prefix.
 */
public class Download {
    
    public static void download( AmazonS3 s3, 
				 String bucket,
				 String key ) 
	throws IOException {
	File file = new File( key );
	s3.getObject( new GetObjectRequest( bucket, key ),
		      file );
	if ( file.length() == 0 ) {
	    file.delete();
	    file.mkdir();
	}
    }
    
    public static void downloadPrefix( AmazonS3 s3,
				       String bucket,
				       String prefix ) 
	throws IOException {
	for( String key : ListBucket.listBucket( s3, 
						 bucket, 
						 prefix ) ) {
	    download( s3,
		      bucket,
		      key );
	}
    }

    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Needs a bucket name" );
	    System.exit( 1 );
	}
	try {
	    AmazonS3 s3 = CredentialParameters.makeParameters().getS3();
	    downloadPrefix( s3,
			    args[ 0 ],
			    "" );
	} catch( Exception e ) {
	    e.printStackTrace();
	    System.err.println( e );
	}
    }
}