package com.bielskyi.currencysystem.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class UtilsTest {

    @Test
    public void testGetLocalDateTimeFromString() {
        LocalDateTime expected = LocalDateTime.of(2023, 5, 1, 0, 0, 0);
        LocalDateTime result = Utils.getLocalDateTimeFromString("01.05.2023", Utils.FORMATTER_DATE_PATTERN);
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void testGetStringDateByPattern() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 5, 1, 0, 0, 0);
        String expected = "01.05.2023";
        String result = Utils.getStringDateByPattern(dateTime, Utils.FORMATTER_DATE_PATTERN);
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void testGetRate() {
        float buyRate = 1.0f;
        float sellRate = 2.0f;
        float expected = 1.5f;
        float result = Utils.getRate(buyRate, sellRate);
        Assertions.assertEquals(expected, result);
    }
}

