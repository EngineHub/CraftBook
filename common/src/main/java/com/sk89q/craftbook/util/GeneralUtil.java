package com.sk89q.craftbook.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class GeneralUtil {

    private GeneralUtil () {
    }

    public static String getStackTrace (Throwable ex) {

        Writer out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        ex.printStackTrace(pw);
        return out.toString();
    }
}