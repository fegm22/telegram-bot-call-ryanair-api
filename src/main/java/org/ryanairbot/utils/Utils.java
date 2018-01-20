package org.ryanairbot.utils;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {

    public static LocalDateTime covertLocalDateTime(Integer year, Integer month, Integer day, String hour) {
        String formatDay = String.format("%02d", Integer.parseInt(day.toString()));
        String formatMonth = String.format("%02d", Integer.parseInt(month.toString()));
        return covertLocalDateTime(year + "-" + formatMonth + "-" + formatDay + "T" + hour);
    }

    public static LocalDateTime covertLocalDateTime(String date) {
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }
}
