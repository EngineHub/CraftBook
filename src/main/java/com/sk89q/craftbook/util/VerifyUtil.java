package com.sk89q.craftbook.util;

public class VerifyUtil {

    public static void verifyRadius(int radius, int maxradius) {

        if (radius < 0)
            radius = 0;

        if (radius > maxradius)
            radius = maxradius;
    }
}