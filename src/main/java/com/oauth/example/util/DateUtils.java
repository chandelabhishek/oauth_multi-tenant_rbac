package com.oauth.example.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtils {
    public static String getMonthYear(int interval) {
        var cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + interval);
        SimpleDateFormat ft = new SimpleDateFormat("MM/yy");
        return ft.format(cal.getTime());
    }
}
