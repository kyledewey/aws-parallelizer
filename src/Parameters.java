import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;


/**
 * Base class for parameters.
 * Provides a default implementation of the makeParams routine.
 * @author Kyle Dewey
 */
public abstract class Parameters {
    // begin constants
    // delimeter for making error messages
    public static final String ERROR_DELIM = ", ";
    public static final String MAP_DELIM = "=";
    public static final String DEFAULT_PARAMETERS_FILE = "parameters.txt";
    // end constants

    // begin instance variables
    protected Map< String, String > params;
    // end instance variables

    public Parameters( Map< String, String > input ) throws ParameterException {
	params = makeParams( input );
    }

    /**
     * Returns the value of the given key, or null if the key is not recognized.
     */
    public String param( String key ) {
	return params.containsKey( key ) ? params.get( key ) : null;
    }

    /**
     * Validates that all the given keys were entered and adds any optional keys
     */
    protected Map< String, String > makeParams( Map< String, String > input ) 
	throws ParameterException {
	validateInput( input );
	return fillMissing( input );
    }

    /**
     * Makes sure that the given input has all the needed keys.
     * Any extra keys are ignored.
     */
    public void validateInput( Map< String, String > input )
	throws ParameterException {
	Set< String > missing = setDiff( getNeededParams(),
					 input.keySet() );
	if ( missing.size() > 0 ) {
	    List< String > missingSorted = new ArrayList< String >( missing );
	    Collections.sort( missingSorted );
	    throw new ParameterException( "Missing needed parameters: " +
					  join( missingSorted, ERROR_DELIM ) );
	}
    }

    /**
     * Given a parameters object, it fills in
     * any missing keys, returning a Map with all the needed parameters
     * accounted for.
     */
    public Map< String, String > fillMissing( Map< String, String > input ) {
	Map< String, String > retval = new HashMap< String, String >( input );
	Map< String, String > optional = getOptionalParams();
	for ( String missing : setDiff( optional.keySet(), input.keySet() ) ) {
	    retval.put( missing, optional.get( missing ) );
	}
	return retval;
    }

    public Set< String > knownKeys() {
	return setUnion( getNeededParams(),
			 getOptionalParams().keySet() );
    }

    /**
     * Removes any keys we don't know about in the given params object.
     */
    public Map< String, String > stripUnknownKeys( Map< String, String > input ) {
	Map< String, String > retval = 
	    new HashMap< String, String >( input );
	for( String extra : setDiff( input.keySet(), knownKeys() ) ) {
	    retval.remove( extra );
	}
	return retval;
    }

    /**
     * Gets the difference of two sets, non-destructively.
     */
    public static < T > Set< T > setDiff( Set< T > first,
					  Set< T > second ) {
	Set< T > retval = new HashSet< T >( first );
	retval.removeAll( second );
	return retval;
    }

    public static < T > Set< T > setUnion( Set< T > first,
					   Set< T > second ) {
	Set< T > retval = new HashSet< T >( first );
	retval.addAll( second );
	return retval;
    }

    public static String join( List< String > items, String delim ) {
	String retval = "";
	for( String item : items ) {
	    retval += item + delim;
	}
	return retval.substring( 0, retval.length() - delim.length() );
    }

    public static boolean allWhitespace( String string ) {
	for( int x = 0; x < string.length(); x++ ) {
	    if ( !Character.isWhitespace( string.charAt( x ) ) ) {
		return false;
	    }
	}
	return true;
    }

    /**
     * @return a new key/pair mapping, or null if the line was blank.
     * @throws ParameterException If the line is malformed
     */
    public static Pair< String, String > parseLine( String line )
	throws ParameterException {
	String[] split = line.split( MAP_DELIM );
	if ( split.length == 1 ) {
	    if ( !allWhitespace( split[ 0 ] ) ) {
		throw new ParameterException( "Non-blank line missing value" );
	    }
	    return null;
	} else if ( split.length == 2 ) {
	    return new Pair< String, String >( split[ 0 ], split[ 1 ] );
	} else {
	    throw new ParameterException( "Extra key/pair mappings on the same line" );
	}
    }

    public static Map< String, String > readMapFromFile() 
	throws IOException, ParameterException {
	return readMapFromFile( DEFAULT_PARAMETERS_FILE );
    }

    public static Map< String, String > readMapFromFile( String filename ) 
	throws IOException, ParameterException {
	BufferedReader reader =
	    new BufferedReader( new FileReader( filename ) );
	try {
	    return readMapFromBufferedReader( 
		     new BufferedReader( new FileReader( filename ) ) );
	} finally {
	    reader.close();
	}
    }

    public static Map< String, String > readMapFromBufferedReader( BufferedReader reader )
	throws IOException, ParameterException {
	String line;
	Map< String, String > retval = new HashMap< String, String >();
	while( ( line = reader.readLine() ) != null ) {
	    Pair< String, String > pair = parseLine( line );
	    if ( pair != null ) {
		retval.put( pair.first, pair.second );
	    }
	}
	return retval;
    }

    public static String paramLine( Map.Entry< String, String > entry ) {
	return entry.getKey() + MAP_DELIM + entry.getValue();
    }

    /**
     * Gets a copy of all parameters.
     */
    public Map< String, String > getAllParameters() {
	return new HashMap< String, String >( params );
    }

    /**
     * @param beMinimal true if you want it to only show params specific to
     * this kind of parameter, else false.
     */
    public List< String > asLines( boolean beMinimal ) {
	Set< String > known = null;
	if ( beMinimal ) {
	    known = knownKeys();
	}
	List< String > retval = new ArrayList< String >();
	for ( Map.Entry< String, String > entry : params.entrySet() ) {
	    if ( ( beMinimal && known.contains( entry.getKey() ) ) ||
		 !beMinimal ) {
		retval.add( paramLine( entry ) );
	    }
	}
	return retval;
    }

    public String toMinimalString() {
	return join( asLines( true ), "\n" );
    }

    public String toString() {
	return join( asLines( false ), "\n" );
    }

    public abstract Set< String > getNeededParams();
    public abstract Map< String, String > getOptionalParams();
}