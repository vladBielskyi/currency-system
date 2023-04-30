package com.bielskyi.currencysystem.service.impl;

import com.bielskyi.currencysystem.dto.FetchedCurrencyDataDto;
import com.bielskyi.currencysystem.dto.sources.MonoCurrencyDto;
import com.bielskyi.currencysystem.entity.CurrencyData;
import com.bielskyi.currencysystem.exception.ApiRetrieveException;
import com.bielskyi.currencysystem.exception.InternalException;
import com.bielskyi.currencysystem.service.CurrencyExchangerService;
import com.bielskyi.currencysystem.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonoDataServiceImpl implements CurrencyExchangerService {

    @Value(value = "${com.currency-system.currency.mono_api}")
    private String CURRENCY_MONO_API;

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<FetchedCurrencyDataDto> getCurrencyDataForThePeriod(LocalDateTime date) {
        MonoCurrencyDto[] monoData = fetchMonoCurrencies();
        return buildFetchedCurrencyDataListForThePeriod(monoData, Utils.getTimestamp(date));
    }

    @Override
    public CurrencyData.SourceName getSourceName() {
        return CurrencyData.SourceName.MONOBANK;
    }

    private MonoCurrencyDto[] fetchMonoCurrencies() {
        Request request = new Request.Builder()
                .url(CURRENCY_MONO_API)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Error while fetching monobank api: {}", response);
                throw new ApiRetrieveException(String.format("Unexpected response code while fetching monobank api: %s",
                        response.code()));
            }

            return objectMapper.readValue(response.body().string(), MonoCurrencyDto[].class);
        } catch (IOException e) {
            log.error("Error while parsing data: {}", e);
            throw new InternalException("Something went wrong while parsing data", e);
        }
    }

    private List<FetchedCurrencyDataDto> buildFetchedCurrencyDataListForThePeriod(MonoCurrencyDto[] monoDataDto, long date) {
        return Arrays.stream(monoDataDto)
                .filter(x -> Instant.ofEpochSecond(x.getDate()).atZone(ZoneOffset.UTC).toLocalDate()
                        .isEqual(Instant.ofEpochSecond(date).atZone(ZoneOffset.UTC).toLocalDate()))
                .map(dataDto -> mapFetchedCurrencyDto(dataDto))
                .collect(Collectors.toList());
    }

    private FetchedCurrencyDataDto mapFetchedCurrencyDto(MonoCurrencyDto dataDto) {
        boolean hasRate = dataDto.getRateBuy() != 0 && dataDto.getRateSell() != 0;
        return FetchedCurrencyDataDto.builder()
                .baseCurrencyCode(dataDto.getCurrencyCodeB())
                .currencyCode(dataDto.getCurrencyCodeA())
                .date(LocalDateTime.ofEpochSecond(dataDto.getDate(), 0, java.time.ZoneOffset.UTC))
                .buyRate(hasRate ? dataDto.getRateBuy() : 0)
                .sellRate(hasRate ? dataDto.getRateSell() : 0)
                .rate(hasRate ? Utils.getRate(dataDto.getRateBuy(), dataDto.getRateSell()) : dataDto.getRateCross())
                .source(getSourceName().getValue())
                .build();
    }
}
