package com.sk89q.craftbook;

/**
 * Parent type for all exceptions specific to Craftbook.
 * 
 * @author hash
 * 
 */
public class CraftbookException extends Exception {
    public CraftbookException() {
        super();
    }
    
    public CraftbookException(String $message, Throwable $cause) {
        super($message, $cause);
    }
    
    public CraftbookException(String $message) {
        super($message);
    }
    
    public CraftbookException(Throwable $cause) {
        super($cause);
    }
}
