package com.bielskyi.currencysystem.service;

import com.bielskyi.currencysystem.dto.FetchedCurrencyDataDto;
import com.bielskyi.currencysystem.entity.CurrencyData;

import java.time.LocalDateTime;
import java.util.List;

public interface CurrencyExchangerService {

    List<FetchedCurrencyDataDto> getCurrencyDataForThePeriod(LocalDateTime date);

    CurrencyData.SourceName getSourceName();
}
