package com.bielskyi.currencysystem.service.impl;

import com.bielskyi.currencysystem.dto.report.ExchangeRatesReport;
import com.bielskyi.currencysystem.repository.CurrencyDataRepository;
import com.bielskyi.currencysystem.service.CurrencySourceFactory;
import com.bielskyi.currencysystem.service.impl.CurrencyDataServiceImpl;
import com.bielskyi.currencysystem.service.redis.CacheService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CurrencyDataServiceImplTest {

    @Mock
    private CurrencyDataRepository currencyDataRepository;

    @Mock
    private CurrencySourceFactory currencySourceFactory;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private CurrencyDataServiceImpl currencyDataService;

    @Test
    public void testGetExchangeRatesForPeriodReport_fromCache() {
        LocalDateTime date = LocalDateTime.of(2022, Month.JANUARY, 1, 0, 0, 0);
        ExchangeRatesReport report = new ExchangeRatesReport();
        report.setErrors(new ArrayList<>());
        Mockito.when(cacheService.get(Mockito.anyString(), Mockito.eq(ExchangeRatesReport.class)))
                .thenReturn(Optional.of(report));

        ExchangeRatesReport result = currencyDataService.getExchangeRatesForPeriodReport(date);

        Mockito.verify(cacheService).get(Mockito.anyString(), Mockito.eq(ExchangeRatesReport.class));
        Mockito.verifyNoMoreInteractions(currencyDataRepository, currencySourceFactory, cacheService);
        Assertions.assertSame(report, result);
    }

}