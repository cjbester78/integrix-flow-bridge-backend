package com.integrixs.backend.util.helpers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateHelper {

    /**
     * Format a LocalDateTime to ISO string.
     */
    public String formatISO(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    /**
     * Parse ISO date string to LocalDateTime.
     */
    public LocalDateTime parseISO(String isoDateString) {
        if (isoDateString == null || isoDateString.isEmpty()) return null;
        return LocalDateTime.parse(isoDateString, DateTimeFormatter.ISO_DATE_TIME);
    }

    /**
     * Get current date/time as ISO string.
     */
    public String nowISO() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
