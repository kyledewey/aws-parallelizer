/*
 * ParameterException.java
 *
 * Version:
 *     $Id: ParameterException.java,v 1.1 2012/11/06 01:12:28 kyle Exp $
 *
 * Revisions:
 *      $Log: ParameterException.java,v $
 *      Revision 1.1  2012/11/06 01:12:28  kyle
 *      Initial revision
 *
 *
 */

/**
 * Exception thrown when a parameter is invalid.
 * @author Kyle Dewey
 */
public class ParameterException extends Exception {
    public ParameterException( String message ) {
	super( message );
    }
}
