package com.bielskyi.currencysystem.service;

import com.bielskyi.currencysystem.dto.report.ExchangeRatesReport;

import java.time.LocalDateTime;

public interface CurrencyDataService {

    ExchangeRatesReport getExchangeRatesForPeriodReport(LocalDateTime dateTime);

    ExchangeRatesReport getExchangeRatesReport();

    void importCurrencies();
}
