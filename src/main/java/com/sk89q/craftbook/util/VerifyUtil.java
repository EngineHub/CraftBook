package com.sk89q.craftbook.util;

/**
 * A util file to verify many different things.
 */
public class VerifyUtil {

    /**
     * Verify that a radius is within the maximum.
     * 
     * @param radius The radius to check
     * @param maxradius The maximum possible radius
     * @return The new fixed radius.
     */
    public static int verifyRadius(int radius, int maxradius) {

        if (radius < 0)
            radius = 0;

        if (radius > maxradius)
            radius = maxradius;

        return radius;
    }
}