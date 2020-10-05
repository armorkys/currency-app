package testexample.currencyapiexample.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lt.lb.webservices.fxrates.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
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
@AutoConfigureMockMvc
class MainServiceTest {

    private final XmlMapper xmlMapper = new XmlMapper();
    //@Autowired
    @MockBean
    CurrencyRatesHandlerRepository database;
    @Autowired
    private MockMvc mockMvc;
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
    public void whenHistoricalRatesQueried_ListOfRatesRetrieved_DatesBefore2014() throws DatatypeConfigurationException, JsonProcessingException {
        RestTemplate restTemplate = mainService.restTemplate;
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        FxRatesHandling expectedFxRates = new FxRatesHandling();
        FxRateHandling fxRate = addDataToFxRateHandling(
                "2020-01-01",
                FxRateTypeHandling.EU
        );
            expectedFxRates.getFxRate().add(fxRate);
        String expectedReturnBody = xmlMapper.writeValueAsString(expectedFxRates);
        DateHistoryTemplate urlData = new DateHistoryTemplate(CcyISO4217.EUR, LocalDate.parse("2014-09-30"), LocalDate.parse("2014-09-30"));
        server.expect(ExpectedCount.once(), requestTo(
                URL_MAIN + "getFxRatesForCurrency?tp=EU&ccy=" + urlData.getCcy() +
                        "&dtFrom=" + urlData.getStartDate() + "&dtTo=" + urlData.getEndDate()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedReturnBody, MediaType.APPLICATION_XML));
        FxRatesHandling actualFxRates = mainService.getCurrencyHistory(urlData.getCcy(), LocalDate.parse("2011-01-01"), LocalDate.parse("2012-01-01"));
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
    public void whenCurrentRatesQueried_LBReturnsGoodValues_DBIsEmpty() throws DatatypeConfigurationException {
        List<CurrencyRatesHandler> notEmptyDbList = Arrays.asList(
                new CurrencyRatesHandler(1, "GBP", new BigDecimal("1"), LocalDate.parse("2020-01-01"), "EU"),
                new CurrencyRatesHandler(1, "USD", new BigDecimal("2"), LocalDate.parse("2020-01-01"), "EU"),
                new CurrencyRatesHandler(1, "EUR", new BigDecimal("3"), LocalDate.parse("2020-01-01"), "EU")
        );
        Mockito.when(database.findAll()).thenReturn(notEmptyDbList);
        FxRatesHandling actualFxRates = mainService.getCurrentCurrencyRates();
        assertThat(actualFxRates.getFxRate().size()).isEqualTo(3);
    }

    @Test
    public void updateDBQueried_LBReturns500_ListOfFxRatesIsEmpty() throws JsonProcessingException {
        RestTemplate restTemplate = mainService.restTemplate;
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        FxRatesHandling expectedFxRates = new FxRatesHandling();
        String expectedReturnBody = xmlMapper.writeValueAsString(expectedFxRates);
        server.expect(ExpectedCount.once(), requestTo(URL_MAIN + URL_CURRENCY_CURRENT))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess().body(expectedReturnBody).contentType(MediaType.APPLICATION_XML));
        boolean actualValue = mainService.updateDB();
        server.verify();
        assertThat(actualValue).isEqualTo(false);
    }

    @Test
    public void updateDBQueried_LBReturns500_ListOfFxRatesIsNormalValues() throws DatatypeConfigurationException, JsonProcessingException {
        RestTemplate restTemplate = mainService.restTemplate;
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        FxRatesHandling expectedFxRates = createFxRatesWithListElements(4);
        String expectedReturnBody = xmlMapper.writeValueAsString(expectedFxRates);
        server.expect(ExpectedCount.once(), requestTo(URL_MAIN + URL_CURRENCY_CURRENT))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess().body(expectedReturnBody).contentType(MediaType.APPLICATION_XML));
        boolean actualValue = mainService.updateDB();
        server.verify();
        assertThat(actualValue).isEqualTo(true);
    }

    @Test
    public void getComparatorValuesForCcy_CcyComparatorEmpty(){
        RestTemplate restTemplate = mainService.restTemplate;
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        CcyComparator expectedComparator = new CcyComparator();
        CcyComparator actualComparator = mainService.getComparatorValuesForCcy(expectedComparator);
        assertThat(actualComparator).isEqualTo(expectedComparator);
    }

    @Test
    public void getComparatorValuesForCcy_CcyComparatorNormalValues() {

        CcyComparator expectedComparator = new CcyComparator();
            expectedComparator.setCurrency1(new CcyAmtHandling());
            expectedComparator.setCurrency2(new CcyAmtHandling());
            expectedComparator.getCurrency1().setCcy(CcyISO4217.EUR);
            expectedComparator.getCurrency2().setCcy(CcyISO4217.USD);
            expectedComparator.setAmount(new BigDecimal("10"));
        List<CurrencyRatesHandler> currencyRatesList = Arrays.asList(
                new CurrencyRatesHandler(1, "GPB", new BigDecimal("1"), LocalDate.parse("2020-01-01"), "EU"),
                new CurrencyRatesHandler(2, "USD", new BigDecimal("2"), LocalDate.parse("2020-01-01"), "EU"),
                new CurrencyRatesHandler(3, "AUD", new BigDecimal("3"), LocalDate.parse("2020-01-01"), "EU"),
                new CurrencyRatesHandler(3, "EUR", new BigDecimal("4"), LocalDate.parse("2020-01-01"), "EU")
        );
        Mockito.when(database.findByCcy("USD")).thenReturn(currencyRatesList.get(1));
        Mockito.when(database.findByCcy("EUR")).thenReturn(currencyRatesList.get(3));
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

    private String createDateString(int step) {
        LocalDate date = LocalDate.parse("2020-01-01");
        LocalDate offsetDate = date.plusDays(step);
        return offsetDate.toString();
    }
}