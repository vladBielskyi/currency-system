package com.bielskyi.currencysystem.controller;

import com.bielskyi.currencysystem.dto.report.ExchangeRatesReport;
import com.bielskyi.currencysystem.service.CurrencyDataService;
import com.bielskyi.currencysystem.utils.Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

import static com.bielskyi.currencysystem.utils.Utils.convertToLocalDateTime;

@RestController
@RequestMapping(value = "/api/currencies/reports")
@RequiredArgsConstructor
@Api(value = "Currency reports API")
public class CurrencyRestController {

    private final CurrencyDataService currencyDataService;

    @GetMapping(value = "/rates-actual")
    @ApiOperation(value = "Get exchange report", notes = "Returns report for actual date")
    public ResponseEntity<ExchangeRatesReport> getExchangeRatesReport() {
        ExchangeRatesReport ratesReport = currencyDataService.getExchangeRatesReport();
        return ResponseEntity.ok(ratesReport);
    }

    @GetMapping(value = "/rates-period")
    @ApiOperation(value = "Get exchange report by date", notes = "Returns report for specified date")
    public ResponseEntity<ExchangeRatesReport> getExchangeRatesForPeriodReport(
            @ApiParam(value = "date: accepts a date in the format dd.MM.yyyy", required = true)
            @DateTimeFormat(pattern = Utils.DEFAULT_DATE_PATTER)
            @RequestParam(value = "date") Date date) {
        ExchangeRatesReport periodReport = currencyDataService
                .getExchangeRatesForPeriodReport(convertToLocalDateTime(date));
        return ResponseEntity.ok(periodReport);
    }
}
