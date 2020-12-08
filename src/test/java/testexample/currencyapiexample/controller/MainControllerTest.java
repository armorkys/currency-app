package testexample.currencyapiexample.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lt.lb.webservices.fxrates.CcyAmtHandling;
import lt.lb.webservices.fxrates.CcyISO4217;
import lt.lb.webservices.fxrates.FxRateHandling;
import lt.lb.webservices.fxrates.FxRateTypeHandling;
import lt.lb.webservices.fxrates.FxRatesHandling;
import org.springframework.boot.test.mock.mockito.MockBean;

import testexample.currencyapiexample.service.FxRatesService;

@SpringBootTest
class MainControllerTest {

    @Autowired
    MainController mainController;

    @MockBean
    private FxRatesService fxRatesService;

    @Test
    void getCurrentRatesList() throws DatatypeConfigurationException {

        FxRatesHandling expectedFxRates = createFxRatesWithListElements(2);

        Mockito.when(fxRatesService.getCurrentCurrencyRates()).thenReturn(expectedFxRates);

        FxRatesHandling actualFxRates = mainController.getCurrentRatesList();

        Mockito.verify(fxRatesService, Mockito.times(1)).getCurrentCurrencyRates();
        assertEquals(expectedFxRates, actualFxRates);

    }

    @Test
    void getCurrencyHistory() throws DatatypeConfigurationException {
        FxRatesHandling expectedFxRates = createFxRatesWithListElements(5);

        CcyISO4217 ccy = CcyISO4217.USD;
        LocalDate dateTo = LocalDate.now();
        LocalDate dateFrom = dateTo.minusYears(1);

        Mockito.when(fxRatesService.getCurrencyHistory(ccy, dateFrom, dateTo)).thenReturn(expectedFxRates);

        FxRatesHandling actualFxRates = mainController.getCurrencyHistory(ccy.toString());

        Mockito.verify(fxRatesService, Mockito.times(1)).getCurrencyHistory(ccy, dateFrom, dateTo);
        assertEquals(expectedFxRates, actualFxRates);
    }

    @Test
    void getCurrencyHistoryCustom() throws DatatypeConfigurationException {
        FxRatesHandling expectedFxRates = createFxRatesWithListElements(5);

        CcyISO4217 ccy = CcyISO4217.USD;
        LocalDate dateTo = LocalDate.now();
        LocalDate dateFrom = dateTo.minusYears(1);

        Mockito.when(fxRatesService.getCurrencyHistory(ccy, dateFrom, dateTo)).thenReturn(expectedFxRates);

        FxRatesHandling actualFxRates = mainController.getCurrencyHistoryCustom(ccy.toString(), dateFrom.toString(), dateTo.toString());

        Mockito.verify(fxRatesService, Mockito.times(1)).getCurrencyHistory(ccy, dateFrom, dateTo);
        assertEquals(expectedFxRates, actualFxRates);
    }

    @Test
    void getCurrencyList() {
        List<String> expectedList = new ArrayList<>(Arrays.asList(
                "USD",
                "USD",
                "USD",
                "USD",
                "USD"
        ));

        Mockito.when(fxRatesService.getCurrencyList()).thenReturn(expectedList);

        List<String> actualList = mainController.getCurrencyList();

        Mockito.verify(fxRatesService, Mockito.times(1)).getCurrencyList();
        assertEquals(expectedList, actualList);
    }


    private FxRateHandling addDataToFxRateHandling(String date, FxRateTypeHandling fxRateType)
            throws DatatypeConfigurationException {
        FxRateHandling fxRate1 = new FxRateHandling();
        fxRate1.setDt(DatatypeFactory.newInstance().newXMLGregorianCalendar(date + "T00:00:00.000Z"));
        fxRate1.setTp(fxRateType);
        return fxRate1;
    }

    private FxRatesHandling createFxRatesWithListElements(int amount) throws DatatypeConfigurationException {
        FxRatesHandling fxRatesExpected = new FxRatesHandling();
        for (int step = 0; step < amount; step++) {
            fxRatesExpected.getFxRate().add(addDataToFxRateHandling(createDateString(step), FxRateTypeHandling.EU));
            CcyAmtHandling temp1 = new CcyAmtHandling();
            temp1.setAmt(BigDecimal.ONE);
            temp1.setCcy(CcyISO4217.EUR);
            CcyAmtHandling temp2 = new CcyAmtHandling();
            temp2.setAmt(BigDecimal.valueOf(1.2));
            temp2.setCcy(CcyISO4217.USD);
            fxRatesExpected.getFxRate().get(step).getCcyAmt().add(temp1);
            fxRatesExpected.getFxRate().get(step).getCcyAmt().add(temp2);
        }
        return fxRatesExpected;
    }

    private String createDateString(int step) {
        LocalDate date = LocalDate.parse("2020-01-01");
        LocalDate offsetDate = date.plusDays(step);
        return offsetDate.toString();
    }
}