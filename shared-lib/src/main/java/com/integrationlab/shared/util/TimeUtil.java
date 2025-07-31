package com.integrationlab.shared.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class TimeUtil - auto-generated documentation.
 */
public class TimeUtil {
    /**
     * Method: {()
     */
    public static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}