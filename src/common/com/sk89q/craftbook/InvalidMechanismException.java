package com.sk89q.craftbook;

/**
 * Thrown when a MechanicFactory is considering whether or not to produce a
 * Mechanic and finds that an area of the world looks like it it was intended to
 * be a mechanism, but it is is some way not a valid construction. (For example,
 * an area with a "[Bridge]" sign which has blocks of an inappropriate material,
 * or a sign facing an invalid direction, etc.) It is appropriate to extend this
 * exception to produce more specific types.
 * 
 * @author hash
 * 
 */
public class InvalidMechanismException extends CraftbookException {
    public InvalidMechanismException() {
        super();
    }
    
    public InvalidMechanismException(String $message, Throwable $cause) {
        super($message, $cause);
    }
    
    public InvalidMechanismException(String $message) {
        super($message);
    }
    
    public InvalidMechanismException(Throwable $cause) {
        super($cause);
    }
}
