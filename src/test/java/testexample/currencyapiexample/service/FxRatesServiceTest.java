package testexample.currencyapiexample.service;

import lt.lb.webservices.fxrates.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import testexample.currencyapiexample.repository.FxStorage;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class FxRatesServiceTest {

    @MockBean
    private FxClient fxClient;

    @MockBean
    private FxStorage fxStorage;

    @Autowired
    private FxRatesService fxRatesService;

    @Test
    void getCurrentCurrencyRates() throws DatatypeConfigurationException {

        FxRatesHandling expectedFxRates =  createFxRatesWithListElements(5);

        Mockito.when(fxStorage.getCurrentCurrencyRates())
                .thenReturn(expectedFxRates);

        FxRatesHandling actualFxRates = fxStorage.getCurrentCurrencyRates();

        Mockito.verify(fxStorage, times(1)).getCurrentCurrencyRates();
        assertEquals(expectedFxRates, actualFxRates);

    }

    @Test
    void getCurrencyHistory() throws DatatypeConfigurationException {
        FxRatesHandling expectedFxRates =  createFxRatesWithListElements(5);

        CcyISO4217 ccy = CcyISO4217.USD;
        LocalDate dateFrom = LocalDate.parse("2020-01-02");
        LocalDate dateTo = LocalDate.parse("2020-01-02");

        Mockito.when(fxClient
                .getCurrencyHistory(ccy, dateFrom, dateTo))
                .thenReturn(expectedFxRates);

        FxRatesHandling actualFxRates = fxRatesService
                .getCurrencyHistory(ccy, dateFrom, dateTo);

        Mockito.verify(fxClient, times(1)).getCurrencyHistory(ccy, dateFrom, dateTo);
        assertEquals(expectedFxRates, actualFxRates);

    }

    @Test
    void getCurrencyList() throws DatatypeConfigurationException {
        FxRatesHandling fxRates =  createFxRatesWithListElements(5);

        Mockito.when(fxStorage.getCurrentCurrencyRates()).thenReturn(fxRates);

        List<String> expectedList = new ArrayList<>(Arrays.asList(
                "USD",
                "USD",
                "USD",
                "USD",
                "USD"
        ));
        List<String> actualList = fxRatesService.getCurrencyList();

        Mockito.verify(fxStorage, times(1)).getCurrentCurrencyRates();
        assertLinesMatch(expectedList, actualList);
    }

    private FxRatesHandling createFxRatesEmptyWithErrors(int amount, String errorNumber, String errorDescription)
            throws DatatypeConfigurationException {
        FxRatesHandling expectedFxRates = new FxRatesHandling();
        for (int step = 0; step < amount; step++) {
            OprlErrHandling error = new OprlErrHandling();
            error.setDesc(errorDescription);
            ErrorCode errorCode = new ErrorCode();
            errorCode.setPrtry(errorNumber);
            error.setErr(errorCode);
            expectedFxRates.setOprlErr(error);
        }
        return expectedFxRates;
    }

    private FxRatesHandling createFxRatesWithListElements(int amount) throws DatatypeConfigurationException {
        FxRatesHandling fxRatesExpected = new FxRatesHandling();
        for (int step = 0; step < amount; step++) {
            fxRatesExpected.getFxRate().add(addDataToFxRateHandling(createDateString(step), FxRateTypeHandling.EU));
            CcyAmtHandling temp1 = new CcyAmtHandling();
            temp1.setAmt(new BigDecimal("1"));
            temp1.setCcy(CcyISO4217.EUR);
            CcyAmtHandling temp2 = new CcyAmtHandling();
            temp2.setAmt(new BigDecimal("1.2"));
            temp2.setCcy(CcyISO4217.USD);
            fxRatesExpected.getFxRate().get(step).getCcyAmt().add(temp1);
            fxRatesExpected.getFxRate().get(step).getCcyAmt().add(temp2);
        }
        return fxRatesExpected;
    }

    private FxRateHandling addDataToFxRateHandling(String date, FxRateTypeHandling fxRateType)
            throws DatatypeConfigurationException {
        FxRateHandling fxRate1 = new FxRateHandling();
        fxRate1.setDt(DatatypeFactory.newInstance().newXMLGregorianCalendar(date + "T00:00:00.000Z"));
        fxRate1.setTp(fxRateType);
        return fxRate1;
    }

    private String createDateString(int step) {
        LocalDate date = LocalDate.parse("2020-01-01");
        LocalDate offsetDate = date.plusDays(step);
        return offsetDate.toString();
    }


}