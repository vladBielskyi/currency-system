package com.bielskyi.currencysystem.service.impl;

import com.bielskyi.currencysystem.dto.FetchedCurrencyDataDto;
import com.bielskyi.currencysystem.dto.report.ExchangeRatesReport;
import com.bielskyi.currencysystem.dto.report.Rate;
import com.bielskyi.currencysystem.dto.report.SourceItem;
import com.bielskyi.currencysystem.entity.CurrencyData;
import com.bielskyi.currencysystem.exception.ApiRetrieveException;
import com.bielskyi.currencysystem.repository.CurrencyDataRepository;
import com.bielskyi.currencysystem.service.CurrencyDataService;
import com.bielskyi.currencysystem.service.CurrencyExchangerService;
import com.bielskyi.currencysystem.service.CurrencySourceFactory;
import com.bielskyi.currencysystem.service.redis.CacheService;
import com.bielskyi.currencysystem.utils.RedisUtils;
import com.bielskyi.currencysystem.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyDataServiceImpl implements CurrencyDataService {

    private final CurrencyDataRepository currencyDataRepository;
    private final CurrencySourceFactory currencySourceFactory;
    private final CacheService cacheService;

    @Override
    public ExchangeRatesReport getExchangeRatesForPeriodReport(LocalDateTime date) {
        Optional<ExchangeRatesReport> cacheReport = getDateReportFromCache(date);
        if (cacheReport.isPresent()) {
            return cacheReport.get();
        } else {
            ExchangeRatesReport report = generateExchangeRatesReport(date, Boolean.FALSE);
            if (report.getErrors().isEmpty()) {
                cacheService.set(RedisUtils.getRatesPeriodReportKey(Utils.getTimestamp(date)), report, RedisUtils.ONE_HOUR_MILLIS);
            }
            return report;
        }
    }

    @Override
    public ExchangeRatesReport getExchangeRatesReport() {
        Optional<ExchangeRatesReport> cacheReport = getReportFromCache();
        if (cacheReport.isPresent()) {
            return cacheReport.get();
        } else {
            ExchangeRatesReport report = generateExchangeRatesReport(LocalDateTime.now(), Boolean.TRUE);
            if (report.getErrors().isEmpty()) {
                cacheService.set(RedisUtils.RATES_REPORT_KEY, report, RedisUtils.TWENTY_MINUTES_MILLIS);
            }
            return report;
        }
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 */20 * * * *") // Run every 20 minutes
    public void importCurrencies() {
        log.info("Currency data import started at {}", LocalDateTime.now());

        for (CurrencyData.SourceName source : CurrencyData.SourceName.values()) {
            CurrencyExchangerService exchangerService = currencySourceFactory.findSource(source);

            List<CurrencyData> currencyData = null;

            try {
                currencyData = exchangerService.getCurrencyDataForThePeriod(LocalDateTime.now())
                        .stream()
                        .map(x -> buildCurrencyData(x, true))
                        .collect(Collectors.toList());
            } catch (ApiRetrieveException e) {
                log.error("Error while fetching data {}", e);
            }

            if (currencyData != null && !currencyData.isEmpty()) {
                currencyDataRepository.updateActualToFalseBySource(source.getValue());
                currencyDataRepository.saveAll(currencyData);

                cacheService.delete(Collections.singletonList(RedisUtils.RATES_REPORT_KEY));
            } else {
                log.debug("Currency data for source {} unavailable", source);
            }
        }

        log.info("Currency data import finished at {}", LocalDateTime.now());
    }

    private Optional<ExchangeRatesReport> getReportFromCache() {
        return cacheService.get(RedisUtils.RATES_REPORT_KEY, ExchangeRatesReport.class);
    }

    private Optional<ExchangeRatesReport> getDateReportFromCache(LocalDateTime date) {
        return cacheService.get(RedisUtils.getRatesPeriodReportKey(Utils.getTimestamp(date)), ExchangeRatesReport.class);
    }

    private ExchangeRatesReport generateExchangeRatesReport(LocalDateTime date, boolean actual) {
        List<CurrencyData> reportData = new ArrayList<>();
        List<ExchangeRatesReport.Error> errors = new ArrayList<>();

        for (CurrencyData.SourceName sourceName : CurrencyData.SourceName.values()) {
            List<CurrencyData> currencyData = fetchCurrencyData(sourceName, date, actual, errors);
            reportData.addAll(currencyData);
        }

        ExchangeRatesReport exchangeRatesReport =
                buildExchangeRatesReportFromCurrencyDataList(reportData, OpenDataServiceImpl.UAH_CODE, date);

        exchangeRatesReport.setErrors(errors);

        return exchangeRatesReport;
    }

    private List<CurrencyData> fetchCurrencyData(CurrencyData.SourceName sourceName,
                                                 LocalDateTime date,
                                                 boolean actual,
                                                 List<ExchangeRatesReport.Error> errors) {
        List<CurrencyData> currencyData;
        LocalDateTime startOfTheDay = date.toLocalDate().atStartOfDay();

        if (actual) {
            currencyData = currencyDataRepository.findCurrencyDataByTimestampAndActual(sourceName.getValue(),
                    Boolean.TRUE,
                    Utils.getTimestamp(startOfTheDay),
                    Utils.getTimestamp(startOfTheDay.plusDays(1)));
        } else {
            currencyData = currencyDataRepository.findCurrencyDataByTimestamp(sourceName.getValue(),
                    Utils.getTimestamp(startOfTheDay),
                    Utils.getTimestamp(startOfTheDay.plusDays(1)));
        }

        if (currencyData.isEmpty()) {
            CurrencyExchangerService exchangerService = currencySourceFactory.findSource(sourceName);
            try {
                List<CurrencyData> currencyDataForTheDate = exchangerService.getCurrencyDataForThePeriod(date).stream()
                        .map(x -> buildCurrencyData(x, actual))
                        .collect(Collectors.toList());
                if (!currencyDataForTheDate.isEmpty()) {
                    currencyDataRepository.saveAll(currencyDataForTheDate);
                    currencyData = currencyDataForTheDate;
                }
            } catch (ApiRetrieveException e) {
                log.error("Unexpected response from source: {}", sourceName);
                errors.add(ExchangeRatesReport.Error.builder()
                        .sourceName(sourceName)
                        .message(String.format("Unexpected response from source: %s", sourceName))
                        .build());
            }
        }

        return currencyData;
    }

    private ExchangeRatesReport buildExchangeRatesReportFromCurrencyDataList(List<CurrencyData> currencyDataList,
                                                                             String baseCurrency,
                                                                             LocalDateTime date) {
        Map<String, List<CurrencyData>> currencyInfoList = new HashMap<>();
        Map<String, Rate> ratesMap = new HashMap<>();
        currencyDataList.forEach(x -> currencyInfoList
                .computeIfAbsent(x.getCurrencyCode(), k -> new ArrayList<>()).add(x));

        currencyInfoList.forEach((x, y) -> {
            ratesMap.put(x, new Rate());

            for (CurrencyData currencyData : y) {
                if (currencyData.getBaseCurrencyCode().equals(baseCurrency)) {
                    SourceItem sourceItem = new SourceItem();
                    sourceItem.setSourceName(CurrencyData.SourceName.getSourceName(currencyData.getSource()));
                    sourceItem.setRate(currencyData.getRate());
                    ratesMap.get(x).getSources().add(sourceItem);
                }
            }
        });

        ratesMap.forEach((x, y) -> {
            Float averageRate = 0F;
            for (SourceItem sourceItem : y.getSources()) {
                averageRate += sourceItem.getRate();
            }
            averageRate = averageRate / y.getSources().size();
            ratesMap.get(x).setAverageRate(averageRate);
        });

        return ExchangeRatesReport.builder()
                .baseCurrency(baseCurrency)
                .ratesMap(ratesMap)
                .date(date)
                .build();
    }

    private CurrencyData buildCurrencyData(FetchedCurrencyDataDto fetchedCurrencyDataDto, boolean actual) {
        CurrencyData currencyData = new CurrencyData();
        currencyData.setCurrencyCode(fetchedCurrencyDataDto.getCurrencyCode());
        currencyData.setBaseCurrencyCode(fetchedCurrencyDataDto.getBaseCurrencyCode());
        currencyData.setRate(fetchedCurrencyDataDto.getRate());
        currencyData.setDate(fetchedCurrencyDataDto.getDate());
        currencyData.setSource(fetchedCurrencyDataDto.getSource());
        currencyData.setSellRate(fetchedCurrencyDataDto.getSellRate());
        currencyData.setBuyRate(fetchedCurrencyDataDto.getBuyRate());
        currencyData.setActual(actual);
        return currencyData;
    }
}
