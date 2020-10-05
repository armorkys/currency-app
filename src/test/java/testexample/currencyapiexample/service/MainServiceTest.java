package testexample.currencyapiexample.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lt.lb.webservices.fxrates.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import testexample.currencyapiexample.model.CcyComparator;
import testexample.currencyapiexample.model.CurrencyRatesHandler;
import testexample.currencyapiexample.model.DateHistoryTemplate;
import testexample.currencyapiexample.repository.CurrencyRatesHandlerRepository;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static testexample.currencyapiexample.service.MainService.URL_CURRENCY_CURRENT;
import static testexample.currencyapiexample.service.MainService.URL_MAIN;

@SpringBootTest
class MainServiceTest {

    private final XmlMapper xmlMapper = new XmlMapper();

    @Autowired
    CurrencyRatesHandlerRepository database;

    @Autowired
    private MainService mainService;

    @AfterEach
    void cleanup() {
        database.deleteAll();
    }

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

        FxRateHandling fxRate = addDataToFxRateHandling(
                "2020-01-01",
                FxRateTypeHandling.EU
        );

        expectedFxRates.getFxRate().add(fxRate);

        String expectedReturnBody = xmlMapper.writeValueAsString(expectedFxRates);

        DateHistoryTemplate urlData = new DateHistoryTemplate(CcyISO4217.EUR, LocalDate.parse("2020-01-01"), LocalDate.parse("2020-02-01"));

        server.expect(ExpectedCount.once(), requestTo(URL_MAIN + "getFxRatesForCurrency?tp=EU&ccy=" + urlData.getCcy() +
                "&dtFrom=" + urlData.getStartDate() + "&dtTo=" + urlData.getEndDate()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError().body(expectedReturnBody).contentType(MediaType.APPLICATION_XML));

        FxRatesHandling actualFxRates = mainService.getCurrencyHistory(urlData.getCcy(), urlData.getStartDate(), urlData.getEndDate());

        server.verify();
        assertThat(actualFxRates.getFxRate()).isEmpty();
        assertThat(actualFxRates.getOprlErr().getErr().getPrtry()).isEqualTo(expectedFxRates.getOprlErr().getErr().getPrtry());
    }

    @Test
    public void whenCurrentRatesQueried_LBReturns500_ListOfFxRatesIsEmpty() throws DatatypeConfigurationException, JsonProcessingException {
        RestTemplate restTemplate = mainService.restTemplate;
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        FxRatesHandling expectedFxRates = new FxRatesHandling();

        OprlErrHandling error = new OprlErrHandling();
        error.setDesc("Expecting this error in test");
        ErrorCode errorCode = new ErrorCode();
        errorCode.setPrtry("500");
        error.setErr(errorCode);
        expectedFxRates.setOprlErr(error);

        FxRateHandling fxRate = addDataToFxRateHandling(
                "2020-01-01",
                FxRateTypeHandling.EU
        );

        expectedFxRates.getFxRate().add(fxRate);

        String expectedReturnBody = xmlMapper.writeValueAsString(expectedFxRates);

        server.expect(ExpectedCount.once(), requestTo(URL_MAIN + URL_CURRENCY_CURRENT))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError().body(expectedReturnBody).contentType(MediaType.APPLICATION_XML));

        FxRatesHandling actualFxRates = mainService.getCurrentCurrencyRates();

        server.verify();
        assertThat(actualFxRates.getFxRate()).isEmpty();
        assertThat(actualFxRates.getOprlErr().getErr().getPrtry()).isEqualTo(expectedFxRates.getOprlErr().getErr().getPrtry());
    }

    @Test
    @Disabled
    public void updateDBQueried_LBReturns500_ListOfFxRatesIsEmpty() throws DatatypeConfigurationException, JsonProcessingException {
        RestTemplate restTemplate = mainService.restTemplate;
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        FxRatesHandling expectedFxRates = new FxRatesHandling();

        OprlErrHandling error = new OprlErrHandling();
        error.setDesc("Expecting this error in test");
        ErrorCode errorCode = new ErrorCode();
        errorCode.setPrtry("500");
        error.setErr(errorCode);
        expectedFxRates.setOprlErr(error);

        FxRateHandling fxRate = addDataToFxRateHandling(
                "2020-01-01",
                FxRateTypeHandling.EU
        );

        expectedFxRates.getFxRate().add(fxRate);

        String expectedReturnBody = xmlMapper.writeValueAsString(expectedFxRates);


        server.expect(ExpectedCount.once(), requestTo(URL_MAIN + URL_CURRENCY_CURRENT))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError().body(expectedReturnBody).contentType(MediaType.APPLICATION_XML));

        mainService.updateDB();

        server.verify();

    }

    @Test
    public void getComparatorValuesForCcyQueried_CcyComparatorEmpty() throws DatatypeConfigurationException, JsonProcessingException {
        RestTemplate restTemplate = mainService.restTemplate;
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        CcyComparator expectedComparator = new CcyComparator();

        CcyComparator actualComparator = mainService.getComparatorValuesForCcy(expectedComparator);

        assertThat(actualComparator).isEqualTo(expectedComparator);
    }

    @Test
    @Disabled
    public void getComparatorValuesForCcyQueried_CcyComparatorGoodValues() throws DatatypeConfigurationException, JsonProcessingException {
        RestTemplate restTemplate = mainService.restTemplate;
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();


        CcyComparator expectedComparator = new CcyComparator();
        expectedComparator.setCurrency1(new CcyAmtHandling());
        expectedComparator.setCurrency2(new CcyAmtHandling());
        expectedComparator.getCurrency1().setCcy(CcyISO4217.AUD);
        expectedComparator.getCurrency2().setCcy(CcyISO4217.USD);
        expectedComparator.setAmount(new BigDecimal("10"));

        List<CurrencyRatesHandler> dbList = Arrays.asList(
                new CurrencyRatesHandler(1, "GPB", new BigDecimal("1"), LocalDate.parse("2020-01-01"), "EU"),
                new CurrencyRatesHandler(2, "USD", new BigDecimal("2"), LocalDate.parse("2020-01-01"), "EU"),
                new CurrencyRatesHandler(3, "AUD", new BigDecimal("3"), LocalDate.parse("2020-01-01"), "EU"),
                new CurrencyRatesHandler(3, "EUR", new BigDecimal("4"), LocalDate.parse("2020-01-01"), "EU")
        );


        CcyComparator actualComparator = mainService.getComparatorValuesForCcy(expectedComparator);

        assertThat(actualComparator.getAmount()).isEqualTo(expectedComparator.getAmount());
        assertThat(actualComparator.getCurrency1().getCcy()).isEqualTo(expectedComparator.getCurrency1().getCcy());
        assertThat(actualComparator.getCurrency2().getCcy()).isEqualTo(expectedComparator.getCurrency2().getCcy());
        assertThat(actualComparator.getCurrency1().getAmt()).isGreaterThan(new BigDecimal("0"));
        assertThat(actualComparator.getCurrency2().getAmt()).isGreaterThan(new BigDecimal("0"));
    }


    private FxRateHandling addDataToFxRateHandling(String date, FxRateTypeHandling fxRateType) throws DatatypeConfigurationException {
        FxRateHandling fxRate1 = new FxRateHandling();
        fxRate1.setDt(DatatypeFactory.newInstance().newXMLGregorianCalendar(date + "T00:00:00.000Z"));
        fxRate1.setTp(fxRateType);
        return fxRate1;
    }


}