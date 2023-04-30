package com.bielskyi.currencysystem.dto.report;

import com.bielskyi.currencysystem.entity.CurrencyData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SourceItem {
    private Float rate;
    private CurrencyData.SourceName sourceName;
}
