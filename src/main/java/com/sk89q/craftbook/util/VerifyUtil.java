package com.sk89q.craftbook.util;

import java.util.Collection;
import java.util.Collections;

/**
 * A util file to verify many different things.
 */
public final class VerifyUtil {

    /**
     * Verify that a radius is within the maximum.
     * 
     * @param radius The radius to check
     * @param maxradius The maximum possible radius
     * @return The new fixed radius.
     */
    public static double verifyRadius(double radius, double maxradius) {

        return Math.max(0, Math.min(maxradius, radius));
    }

    public static <T> Collection<T> withoutNulls(Collection<T> list) {

        list.removeAll(Collections.singleton(null));

        return list;
    }
}