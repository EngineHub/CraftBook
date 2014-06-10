package com.sk89q.craftbook.util;

public enum TernaryState {

    TRUE,FALSE,NONE;

    public static TernaryState getFromString(String s) {

        s = s.toLowerCase();

        if(s.equals("yes") || s.equals("true") || s.equals("y") || s.equals("t") || s.equals("1"))
            return TRUE;
        if(s.equals("no") || s.equals("false") || s.equals("n") || s.equals("f") || s.equals("0") || s.equals("not"))
            return FALSE;
        return NONE;
    }

    public boolean doesPass(boolean bool) {
        switch (this) {
            case TRUE:
                return bool;
            case FALSE:
                return !bool;
            case NONE:
                return true;
            default:
                return false;
        }
    }
}