package com.bielskyi.currencysystem.controller;

import com.bielskyi.currencysystem.dto.report.ExchangeRatesReport;
import com.bielskyi.currencysystem.service.CurrencyDataService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyRestControllerTest {

    @Mock
    private CurrencyDataService currencyDataService;

    @InjectMocks
    private CurrencyRestController currencyRestController;

    @Test
    @DisplayName("getExchangeRatesReport - should return exchange rates report for actual date")
    void getExchangeRatesReport_ShouldReturnExchangeRatesReportForActualDate() {
        // given
        ExchangeRatesReport expectedReport = new ExchangeRatesReport();
        when(currencyDataService.getExchangeRatesReport()).thenReturn(expectedReport);

        // when
        ResponseEntity<ExchangeRatesReport> response = currencyRestController.getExchangeRatesReport();

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedReport, response.getBody());
        verify(currencyDataService).getExchangeRatesReport();
    }

    @Test
    @DisplayName("getExchangeRatesForPeriodReport - should return exchange rates report for specified date")
    void getExchangeRatesForPeriodReport_ShouldReturnExchangeRatesReportForSpecifiedDate() {
        // given
        ExchangeRatesReport expectedReport = new ExchangeRatesReport();
        Date date = new Date();
        LocalDateTime localDateTime = convertToLocalDateTime(date);
        when(currencyDataService.getExchangeRatesForPeriodReport(localDateTime)).thenReturn(expectedReport);

        // when
        ResponseEntity<ExchangeRatesReport> response = currencyRestController.getExchangeRatesForPeriodReport(date);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedReport, response.getBody());
        verify(currencyDataService).getExchangeRatesForPeriodReport(localDateTime);
    }

    private static LocalDateTime convertToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}

