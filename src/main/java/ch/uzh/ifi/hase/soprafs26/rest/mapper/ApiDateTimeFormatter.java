package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class ApiDateTimeFormatter {

    private ApiDateTimeFormatter() {
    }

    public static String toUtcIsoString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toString();
    }
}
