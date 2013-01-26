import com.amazonaws.*;
import com.amazonaws.auth.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;

import java.io.*;

/**
 * Downloads all the files with the given S3 prefix.
 */
public class Download {

    /**
     * Downloads a particular file in a given bucket with the given key
     * to the given local file.
     */
    public static void download( AmazonS3 s3,
				 String bucket,
				 String key,
				 File file ) 
	throws IOException {
	s3.getObject( new GetObjectRequest( bucket, key ),
		      file );
	if ( file.length() == 0 ) {
	    file.delete();
	    file.mkdir();
	}
    }

    /**
     * Downloads a particular file in a given bucket with the given key
     * to the current directory, using the key as the local filename.
     */
    public static void download( AmazonS3 s3, 
				 String bucket,
				 String key ) 
	throws IOException {
	download( s3, bucket, key, new File( key ) );
    }

    /**
     * Downloads all files with a given prefix to the given directory.
     */
    public static void downloadPrefix( AmazonS3 s3,
				       String bucket,
				       String prefix,
				       String dir ) 
	throws IOException {
	for( String key : ListBucket.listBucket( s3, 
						 bucket, 
						 prefix ) ) {
	    download( s3,
		      bucket,
		      key,
		      new File( dir, key ) );
	}
    }

    public static void main( String[] args ) {
	if ( args.length != 2 ) {
	    System.err.println( "Needs a bucket name and a " + 
				"directory where to download files" );
	    System.exit( 1 );
	}
	File asDir = new File( args[ 1 ] );
	if ( !asDir.exists() ) {
	    if ( !asDir.mkdir() ) {
		System.err.println( "Could not make directory: " + args[ 1 ] );
		System.exit( 1 );
	    }
	} else if ( !asDir.isDirectory() ) {
	    System.err.println( "Download path is not a directory: " + args[ 1 ] );
	    System.exit( 1 );
	}
	try {
	    AmazonS3 s3 = CredentialParameters.makeParameters().getS3();
	    downloadPrefix( s3,
			    args[ 0 ],
			    "",
			    args[ 1 ] );
	} catch( Exception e ) {
	    e.printStackTrace();
	    System.err.println( e );
	}
    }
}