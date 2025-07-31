package com.integrationlab.util.helpers;

public class StringUtils {

    /**
     * Returns true if the string is null or empty.
     */
    public boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * Convert string to uppercase.
     */
    public String toUpperCase(String s) {
        return s == null ? null : s.toUpperCase();
    }

    /**
     * Convert string to lowercase.
     */
    public String toLowerCase(String s) {
        return s == null ? null : s.toLowerCase();
    }
}
