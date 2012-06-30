package com.sk89q.craftbook.util;

/**
 * @author Silthus
 */
public final class EnumUtil {

    // util class
    private EnumUtil() {}

    /**
     * Get the enum value of a string, null if it doesn't exist.
     */
    public static <T extends Enum<T>> T getEnumFromString(Class<T> c, String string) {
	if (c != null && string != null) {
	    try {
		return Enum.valueOf(c, string.trim().toUpperCase());
	    } catch (IllegalArgumentException ex) {
	    }
	}
	return null;
    }

    /**
     * Get the enum value of a string, null if it doesn't exist.
     */
    public static <T extends Enum<T>> T getEnumFromStringCaseSensitive(Class<T> c, String string) {
	if (c != null && string != null) {
	    try {
		return Enum.valueOf(c, string);
	    } catch (IllegalArgumentException ex) {
	    }
	}
	return null;
    }
}
