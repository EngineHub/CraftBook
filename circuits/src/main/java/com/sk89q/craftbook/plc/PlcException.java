package com.sk89q.craftbook.plc;

public class PlcException extends Exception {
    public PlcException() {
    }

    public PlcException(String message) {
        super(message);
    }

    public PlcException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlcException(Throwable cause) {
        super(cause);
    }

    public PlcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
