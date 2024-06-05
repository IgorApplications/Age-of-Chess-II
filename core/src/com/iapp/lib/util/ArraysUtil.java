package com.iapp.lib.util;

/**
 * Utility class for working with arrays
 * */
public class ArraysUtil {

    public static int indexOf(Object[] objects, Object obj) {
        for (int i = 0; i < objects.length; i++) {
            if (objects[i].equals(obj)) {
                return i;
            }
        }
        return -1;
    }
}
