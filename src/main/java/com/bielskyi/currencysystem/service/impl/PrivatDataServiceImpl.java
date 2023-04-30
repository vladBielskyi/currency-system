package com.bielskyi.currencysystem.service.impl;

import com.bielskyi.currencysystem.dto.FetchedCurrencyDataDto;
import com.bielskyi.currencysystem.dto.sources.PrivatBankExchangeRatesDto;
import com.bielskyi.currencysystem.dto.sources.PrivatCurrencyDto;
import com.bielskyi.currencysystem.entity.CurrencyData;
import com.bielskyi.currencysystem.exception.ApiRetrieveException;
import com.bielskyi.currencysystem.exception.InternalException;
import com.bielskyi.currencysystem.service.CurrencyExchangerService;
import com.bielskyi.currencysystem.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrivatDataServiceImpl implements CurrencyExchangerService {

    @Value(value = "${com.currency-system.currency.privat_api}")
    private String CURRENCY_PRIVAT_API;

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<FetchedCurrencyDataDto> getCurrencyDataForThePeriod(LocalDateTime date) {
        return buildFetchedCurrencyDataList(fetchPrivatCurrencies(date));
    }

    @Override
    public CurrencyData.SourceName getSourceName() {
        return CurrencyData.SourceName.PRIVATBANK;
    }

    private PrivatBankExchangeRatesDto fetchPrivatCurrencies(LocalDateTime dateTime) {
        HttpUrl url = HttpUrl.parse(CURRENCY_PRIVAT_API)
                .newBuilder()
                .addQueryParameter("date", Utils.getStringDateByPattern(dateTime, Utils.FORMATTER_DATE_PATTERN))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Error while fetching privat bank api: {}", response);
                throw new ApiRetrieveException(String.format("Unexpected response code while fetching privatbank api: %s",
                        response.code()));
            }

            return objectMapper.readValue(response.body().string(), PrivatBankExchangeRatesDto.class);
        } catch (IOException e) {
            log.error("Error while parsing data: {}", e);
            throw new InternalException("Something went wrong while parsing data", e);
        }
    }

    private List<FetchedCurrencyDataDto> buildFetchedCurrencyDataList(PrivatBankExchangeRatesDto privatBankExchangeRatesDto) {
        return privatBankExchangeRatesDto.getExchangeRate().stream()
                .map(dataDto -> mapFetchedCurrencyDto(dataDto, privatBankExchangeRatesDto.getDate()))
                .collect(Collectors.toList());
    }

    private FetchedCurrencyDataDto mapFetchedCurrencyDto(PrivatCurrencyDto dataDto, String date) {
        boolean hasRate = dataDto.getSaleRate() != 0 && dataDto.getPurchaseRate() != 0;
        return FetchedCurrencyDataDto.builder()
                .baseCurrencyCode(String.valueOf(Utils.currencyCodes.get(dataDto.getBaseCurrency())))
                .currencyCode(String.valueOf(Utils.currencyCodes.get(dataDto.getCurrency())))
                .buyRate(dataDto.getPurchaseRate() == 0 ? dataDto.getPurchaseRateNB() : dataDto.getPurchaseRate())
                .sellRate(dataDto.getSaleRate() == 0 ? dataDto.getSaleRateNB() : dataDto.getSaleRate())
                .rate(hasRate ? Utils.getRate(dataDto.getPurchaseRate(), dataDto.getSaleRate())
                        : Utils.getRate(dataDto.getSaleRateNB(), dataDto.getSaleRateNB()))
                .date(Utils.getLocalDateTimeFromString(date, Utils.FORMATTER_DATE_PATTERN))
                .source(getSourceName().getValue())
                .build();
    }
}
