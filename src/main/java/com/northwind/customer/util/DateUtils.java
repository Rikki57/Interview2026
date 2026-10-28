package com.northwind.customer.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateUtils {

    public static final SimpleDateFormat DEFAULT_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private DateUtils() {
    }

    public static String format(Date date) {
        if (date == null) {
            return null;
        }
        return DEFAULT_FORMAT.format(date);
    }
}
