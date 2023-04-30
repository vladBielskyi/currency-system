package com.bielskyi.currencysystem.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class RedisUtilsTest {

    @Test
    void getRatesPeriodReportKey_returnsCorrectKey() {
        long date = 1648972800000L; // April 2, 2022 12:00:00 AM UTC
        String expectedKey = "rates:report:date:1648972800000";

        String actualKey = RedisUtils.getRatesPeriodReportKey(date);

        assertThat(actualKey).isEqualTo(expectedKey);
    }

    @Test
    void getRatesPeriodReportKey_returnsEmptyStringForZeroDate() {
        long date = 0L;
        String expectedKey = "rates:report:date:0";

        String actualKey = RedisUtils.getRatesPeriodReportKey(date);

        assertThat(actualKey).isEqualTo(expectedKey);
    }

    @Test
    void getRatesPeriodReportKey_returnsEmptyStringForNegativeDate() {
        long date = -1648972800000L; // April 2, 1914 12:00:00 AM UTC
        String expectedKey = "rates:report:date:-1648972800000";

        String actualKey = RedisUtils.getRatesPeriodReportKey(date);

        assertThat(actualKey).isEqualTo(expectedKey);
    }
}

