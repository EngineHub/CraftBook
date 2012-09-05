package com.sk89q.craftbook.plc;

public class PlcException extends Exception {
    private static final long serialVersionUID = -3541787352186665688L;

    public final String detailedMessage;

    public PlcException(String message) {
        super(message);
        this.detailedMessage = message;
    }
    public PlcException(String message, String detailedMessage) {
        super(message);
        this.detailedMessage = detailedMessage;
    }
}
