/*
 * Pair.java
 *
 * Version:
 *     $Id: Pair.java,v 1.1 2012/11/06 01:12:28 kyle Exp $
 *
 * Revisions:
 *      $Log: Pair.java,v $
 *      Revision 1.1  2012/11/06 01:12:28  kyle
 *      Initial revision
 *
 *
 */

/**
 * A simple pair, because Java doesn't have one.
 * Note that it is immutable.
 * @author Kyle Dewey
 */
public class Pair< T, U > {
    // begin instance variables
    public final T first;
    public final U second;
    // end instance variables

    /**
     * Creates a new pair with the given values for first and second
     */
    public Pair( T first, U second ) {
	this.first = first;
	this.second = second;
    }
}
