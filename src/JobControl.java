import java.io.*;

/**
 * Contains various helper routines for running processes
 * and general UNIX environment integration.
 * @author Kyle Dewey
 */
public class JobControl {
    /**
     * Returns the output of the program in a single string.
     */
    public static String executeProgram( String[] args ) 
	throws IOException {
	String retval = "";
	String line;
	Process process = 
	    Runtime.getRuntime().exec( args );
	InputStream inputStream = process.getInputStream();
	BufferedReader reader =
	    new BufferedReader( new InputStreamReader( inputStream ) );
	while ( ( line = reader.readLine() ) != null ) {
	    retval += line;
	}
	reader.close();
	inputStream.close();
	process.getOutputStream().close();
	process.getErrorStream().close();
	return retval;
    }

    /**
     * Makes the file encoded by the given path executable.
     * Refers to the local disk.
     */
    public static void makeExecutable( String fileName ) throws IOException {
	executeProgram( new String[]{ "chmod",
				      "a+x",
				      fileName } );
    }

    /**
     * Makes all files in the given directory path executable
     * Refers to the local disk.
     */
    public static void makeExecutableInDir( String dirName ) throws IOException {
	for( File current : new File( dirName ).listFiles() ) {
	    makeExecutable( current.getPath() );
	}
    }
}
