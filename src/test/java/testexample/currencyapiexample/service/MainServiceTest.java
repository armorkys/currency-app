package testexample.currencyapiexample.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lt.lb.webservices.fxrates.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import testexample.currencyapiexample.model.CurrencyRatesHandler;
import testexample.currencyapiexample.model.DateHistoryTemplate;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static testexample.currencyapiexample.service.MainService.URL_CURRENCY_HISTORY;
import static testexample.currencyapiexample.service.MainService.URL_MAIN;

@SpringBootTest
class MainServiceTest {

    @Autowired
    private MainService mainService;
    private final XmlMapper xmlMapper = new XmlMapper();

    @Test
    public void whenHistoricalRatesQueried_ListOfRatesRetrieved() throws DatatypeConfigurationException, JsonProcessingException {
        RestTemplate restTemplate = mainService.restTemplate;
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        FxRatesHandling expectedFxRates = new FxRatesHandling();
        FxRateHandling fxRate = addDataToFxRateHandling(
                "2020-01-01",
                FxRateTypeHandling.EU
        );

        expectedFxRates.getFxRate().add(fxRate);
        String expectedReturnBody = xmlMapper.writeValueAsString(expectedFxRates);

        DateHistoryTemplate urlData = new DateHistoryTemplate(CcyISO4217.EUR, LocalDate.parse("2020-01-01"), LocalDate.parse("2020-02-01"));

        server.expect(ExpectedCount.once(), requestTo(
                URL_MAIN + "getFxRatesForCurrency?tp=EU&ccy=" + urlData.getCcy() +
                        "&dtFrom=" + urlData.getStartDate() + "&dtTo=" + urlData.getEndDate()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedReturnBody, MediaType.APPLICATION_XML));

        FxRatesHandling actualFxRates = mainService.getCurrencyHistory(urlData.getCcy(), urlData.getStartDate(), urlData.getEndDate());

        server.verify();
        assertThat(actualFxRates.getFxRate()).hasSize(1);
        assertThat(actualFxRates.getFxRate().get(0).getCcyAmt()).isEqualTo(expectedFxRates.getFxRate().get(0).getCcyAmt());
        assertThat(actualFxRates.getFxRate().get(0).getDt()).isEqualTo(expectedFxRates.getFxRate().get(0).getDt());
        assertThat(actualFxRates.getFxRate().get(0).getTp()).isEqualTo(expectedFxRates.getFxRate().get(0).getTp());
        assertThat(actualFxRates.getOprlErr()).isEqualTo(expectedFxRates.getOprlErr());
    }

    private FxRateHandling addDataToFxRateHandling(String date, FxRateTypeHandling fxRateType) throws DatatypeConfigurationException {
        FxRateHandling fxRate1 = new FxRateHandling();
        fxRate1.setDt(DatatypeFactory.newInstance().newXMLGregorianCalendar(date + "T00:00:00.000Z"));
        fxRate1.setTp(fxRateType);
        return fxRate1;
    }





    @Test
    public void whenHistoricalRatesQueried_LBReturns500_ListOfFxRatesIsEmpty() throws DatatypeConfigurationException, JsonProcessingException {
        RestTemplate restTemplate = mainService.restTemplate;
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        FxRatesHandling expectedFxRates = new FxRatesHandling();

        OprlErrHandling error = new OprlErrHandling();
        error.setDesc("Expecting this error in test");
        ErrorCode errorCode = new ErrorCode();
        errorCode.setPrtry("500");
        error.setErr(errorCode);
        expectedFxRates.setOprlErr(error);

        String expectedReturnBody = xmlMapper.writeValueAsString(expectedFxRates);

        DateHistoryTemplate urlData = new DateHistoryTemplate(CcyISO4217.EUR, LocalDate.parse("2020-01-01"), LocalDate.parse("2020-02-01"));

        server.expect(ExpectedCount.once(), requestTo(URL_MAIN + "getFxRatesForCurrency?tp=EU&ccy=" + urlData.getCcy() +
                "&dtFrom=" + urlData.getStartDate() + "&dtTo=" + urlData.getEndDate()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError().body(expectedReturnBody));

        FxRatesHandling actualFxRates = mainService.getCurrencyHistory(urlData.getCcy(), urlData.getStartDate(), urlData.getEndDate());

        server.verify();
        assertThat(actualFxRates.getFxRate()).isEmpty();
        assertThat(actualFxRates.getOprlErr()).isEqualTo(expectedFxRates.getOprlErr());
    }
}