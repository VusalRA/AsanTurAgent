package com.example.asanturagent.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CalculateEndDate {

    public static LocalDateTime getDeadLine(LocalTime currentTime, String startTime, String endTime, Integer duration) {
        LocalDateTime requestDeadline = null;
        if (currentTime.compareTo(LocalTime.parse(startTime)) <= 0) {
            String date = String.valueOf(LocalDate.now());
            String time = String.valueOf(LocalTime.parse(startTime).plusHours(8));
            requestDeadline = LocalDateTime.parse(date + "T" + time);
            return requestDeadline;
        }
        if (currentTime.compareTo(LocalTime.parse(endTime)) >= 0) {
            String date = String.valueOf(LocalDate.now().plusDays(1));
            String time = String.valueOf(LocalTime.parse(startTime).plusHours(8));
            requestDeadline = LocalDateTime.parse(date + "T" + time);
            return requestDeadline;
        } else {
            int c = 8;
            while (c != 0) {
                if (!(currentTime.compareTo(LocalTime.parse(endTime)) > 0)) {
                    currentTime = currentTime.plusHours(1);
                    requestDeadline = LocalDateTime.parse(LocalDate.now() + "T" + currentTime);
                    if (!(LocalTime.parse(currentTime.getHour() + ":00").compareTo(LocalTime.parse(endTime)) == 0)) {
                        c--;
                    }
                } else {
                    String date = String.valueOf(LocalDate.now().plusDays(1));
                    String time;
                    if ((LocalTime.parse(currentTime.getHour() + ":00").compareTo(LocalTime.parse(endTime)) == 0) && c == 1)
                        time = String.valueOf(LocalTime.parse(startTime).plusHours(0).plusMinutes(currentTime.getMinute()));
                    else
                        time = String.valueOf(LocalTime.parse(startTime).plusHours(c - 1).plusMinutes(currentTime.getMinute()));
                    requestDeadline = LocalDateTime.parse(date + "T" + time);
                    break;
                }
            }
            return requestDeadline;
        }
    }
}
