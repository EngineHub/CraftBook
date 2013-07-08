package com.sk89q.craftbook.util;

public enum TernaryState {

    TRUE,FALSE,NONE;

    public static TernaryState getFromString(String s) {

        if(s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("y") || s.equalsIgnoreCase("t") || s.equalsIgnoreCase("1"))
            return TRUE;
        if(s.equalsIgnoreCase("no") || s.equalsIgnoreCase("false") || s.equalsIgnoreCase("n") || s.equalsIgnoreCase("f") || s.equalsIgnoreCase("0"))
            return FALSE;
        return NONE;
    }
}