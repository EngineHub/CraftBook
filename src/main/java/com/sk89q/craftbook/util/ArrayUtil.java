package com.sk89q.craftbook.util;

import java.util.List;

/**
 * @author Silthus
 */
public final class ArrayUtil {

    public static final String[] EMPTY_STRINGS = new String[0];

    /**
     * Turns an ArrayList into an array of the size 8. This new array can be used to output every line in the chat.
     *
     * @param list of string
     * @param page to output
     *
     * @return array of size 8
     */
    public static String[] getArrayPage(List<String> list, int page) {

        if (list.size() < 1) return EMPTY_STRINGS;
        page = Math.abs(page - 1);
        String[] array;
        if (list.size() < 8) {
            array = new String[list.size()];
        } else {
            array = new String[8];
        }
        int j = 0;
        for (int i = page * 8; i < page * 8 + 8; i++) {
            if (list.size() > i) {
                array[j] = list.get(i);
                j++;
            }
        }
        return array;
    }
}
