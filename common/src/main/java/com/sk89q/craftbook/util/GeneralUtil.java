package com.sk89q.craftbook.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class GeneralUtil {

    public static String getStackTrace(Throwable throwable) {

        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        throwable.printStackTrace(printWriter);
        return result.toString();
    }
}
