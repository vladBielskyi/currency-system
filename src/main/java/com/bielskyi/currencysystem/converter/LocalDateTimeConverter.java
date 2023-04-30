package com.bielskyi.currencysystem.converter;


import com.bielskyi.currencysystem.utils.Utils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Converter
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, Long> {
    @Override
    public Long convertToDatabaseColumn(LocalDateTime localDateTime) {
        return Utils.getTimestamp(localDateTime);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Long dbData) {
        if (Objects.isNull(dbData)) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(dbData), ZoneOffset.UTC);
    }
}
