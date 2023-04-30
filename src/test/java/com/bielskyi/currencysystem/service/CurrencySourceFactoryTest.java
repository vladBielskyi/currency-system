package com.bielskyi.currencysystem.service;

import com.bielskyi.currencysystem.entity.CurrencyData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CurrencySourceFactoryTest {

    @Test
    public void findSourceShouldReturnCorrectSource() {
        // Arrange
        Set<CurrencyExchangerService> services = new HashSet<>();
        CurrencyExchangerService source1 = mock(CurrencyExchangerService.class);
        CurrencyExchangerService source2 = mock(CurrencyExchangerService.class);
        when(source1.getSourceName()).thenReturn(CurrencyData.SourceName.MONOBANK);
        when(source2.getSourceName()).thenReturn(CurrencyData.SourceName.PRIVATBANK);
        services.add(source1);
        services.add(source2);
        CurrencySourceFactory factory = new CurrencySourceFactory(services);

        // Act
        CurrencyExchangerService result1 = factory.findSource(CurrencyData.SourceName.MONOBANK);
        CurrencyExchangerService result2 = factory.findSource(CurrencyData.SourceName.PRIVATBANK);

        // Assert
        assertThat(result1).isEqualTo(source1);
        assertThat(result2).isEqualTo(source2);
    }

    @Test
    public void findSourceShouldReturnNullWhenSourceNotFound() {
        // Arrange
        Set<CurrencyExchangerService> services = new HashSet<>();
        CurrencyExchangerService source1 = mock(CurrencyExchangerService.class);
        when(source1.getSourceName()).thenReturn(CurrencyData.SourceName.MONOBANK);
        services.add(source1);
        CurrencySourceFactory factory = new CurrencySourceFactory(services);

        // Act
        CurrencyExchangerService result = factory.findSource(CurrencyData.SourceName.PRIVATBANK);

        // Assert
        assertThat(result).isNull();
    }
}

