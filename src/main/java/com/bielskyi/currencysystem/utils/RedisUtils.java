package com.bielskyi.currencysystem.utils;

public class RedisUtils {

    public static final String RATES_REPORT_KEY = "rates:report";
    private static final String RATES_PERIOD_REPORT_KEY = RATES_REPORT_KEY + ":date:%s";

    public static final Long ONE_HOUR_MILLIS = 3600000L;
    public static final Long TWENTY_MINUTES_MILLIS = ONE_HOUR_MILLIS / 3;

    public static String getRatesPeriodReportKey(long date) {
        return String.format(RATES_PERIOD_REPORT_KEY, String.valueOf(date));
    }

}
