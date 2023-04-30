package com.bielskyi.currencysystem.service.impl;

import com.bielskyi.currencysystem.dto.FetchedCurrencyDataDto;
import com.bielskyi.currencysystem.dto.sources.OpenDataCurrencyDto;
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
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenDataServiceImpl implements CurrencyExchangerService {

    @Value(value = "${com.currency-system.currency.open_data_api}")
    private String CURRENCY_OPEN_DATA_API;
    private static final String JSON_QUERY_PARAM = "&json";
    private static final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static final String UAH_CODE = "980";

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;


    @Override
    public List<FetchedCurrencyDataDto> getCurrencyDataForThePeriod(LocalDateTime date) {
        return buildFetchedCurrencyDataList(fetchOpenDataCurrencies(date));
    }

    @Override
    public CurrencyData.SourceName getSourceName() {
        return CurrencyData.SourceName.OPEN_DATA;
    }

    private OpenDataCurrencyDto[] fetchOpenDataCurrencies(LocalDateTime date) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(CURRENCY_OPEN_DATA_API).newBuilder();
        urlBuilder.addQueryParameter("date", Utils.getStringDateByPattern(date, DATE_PATTERN));
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(url + JSON_QUERY_PARAM)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Error while fetching opendata api: {}", response);
                throw new ApiRetrieveException(String.format("Unexpected response code while fetching open data api: %s",
                        response.code()));
            }

            return objectMapper.readValue(response.body().string(), OpenDataCurrencyDto[].class);
        } catch (IOException e) {
            log.error("Error while parsing data: {}", e);
            throw new InternalException("Something went wrong while parsing data", e);
        }
    }

    private List<FetchedCurrencyDataDto> buildFetchedCurrencyDataList(OpenDataCurrencyDto[] monoDataDto) {
        return Arrays.stream(monoDataDto)
                .map(dataDto -> mapFetchedCurrencyDto(dataDto))
                .collect(Collectors.toList());
    }

    private FetchedCurrencyDataDto mapFetchedCurrencyDto(OpenDataCurrencyDto dataDto) {
        return FetchedCurrencyDataDto.builder()
                .baseCurrencyCode(UAH_CODE)
                .currencyCode(String.valueOf(Utils.currencyCodes.get(dataDto.getAbbreviation())))
                .date(Utils.getLocalDateTimeFromString(dataDto.getExchangeDate(), Utils.FORMATTER_DATE_PATTERN))
                .rate(dataDto.getRate())
                .source(getSourceName().getValue())
                .build();
    }
}
