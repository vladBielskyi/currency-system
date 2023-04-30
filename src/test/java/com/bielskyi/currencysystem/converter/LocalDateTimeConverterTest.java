package com.bielskyi.currencysystem.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class LocalDateTimeConverterTest {

    private final LocalDateTimeConverter converter = new LocalDateTimeConverter();

    @Test
    void convertToDatabaseColumn_shouldReturnCorrectValue() {
        // given
        LocalDateTime dateTime = LocalDateTime.of(2022, 5, 1, 10, 30, 0);
        long expectedValue = 1651401000L;

        // when
        Long result = converter.convertToDatabaseColumn(dateTime);

        // then
        assertEquals(expectedValue, result);
    }

    @Test
    void convertToEntityAttribute_shouldReturnCorrectValue() {
        // given
        Long dbValue = 1651425000L;
        LocalDateTime expectedDateTime = LocalDateTime.of(2022, 5, 1, 17, 10, 0);

        // when
        LocalDateTime result = converter.convertToEntityAttribute(dbValue);

        // then
        assertEquals(expectedDateTime, result);
    }

    @Test
    void convertToEntityAttribute_shouldReturnNullForNullValue() {
        // when
        LocalDateTime result = converter.convertToEntityAttribute(null);

        // then
        assertNull(result);
    }
}
