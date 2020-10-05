package testexample.currencyapiexample.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lt.lb.webservices.fxrates.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.client.RestTemplate;
import testexample.currencyapiexample.model.CcyComparator;
import testexample.currencyapiexample.model.DateHistoryTemplate;
import testexample.currencyapiexample.repository.CurrencyRatesHandlerRepository;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static testexample.currencyapiexample.service.MainService.URL_CURRENCY_CURRENT;
import static testexample.currencyapiexample.service.MainService.URL_MAIN;

@SpringBootTest
class MainControllerTest {

    private final XmlMapper xmlMapper = new XmlMapper();
    @Autowired
    MainController mainController;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    CurrencyRatesHandlerRepository database;

    private MapBindingResult bindingResult;
    private ExtendedModelMap model;
    private MockRestServiceServer server;


    @BeforeEach
    void setup() {
        bindingResult = new MapBindingResult(new HashMap<>(), "undertest");
        model = new ExtendedModelMap();
        server = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @AfterEach
    void cleanup() {
        database.deleteAll();
    }

    @Test
    void getCurrentRatesList() throws DatatypeConfigurationException, JsonProcessingException {
        FxRatesHandling fxRatesExpected = createFxRatesWithListElements(2);
        String expectedReturnBody = xmlMapper.writeValueAsString(fxRatesExpected);
        server.expect(ExpectedCount.once(), requestTo(
                URL_MAIN + URL_CURRENCY_CURRENT))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedReturnBody, MediaType.APPLICATION_XML));

        mainController.getCurrentRatesList(model);

        server.verify();
        List<FxRateHandling> actualCurrencyList = (List<FxRateHandling>) model.getAttribute("currencyList");
        assertThat(actualCurrencyList).hasSize(2);
    }

    @Test
    void getCurrencyHistory() throws DatatypeConfigurationException, JsonProcessingException {
        FxRatesHandling fxRatesExpected = createFxRatesWithListElements(5);
        String expectedReturnBody = xmlMapper.writeValueAsString(fxRatesExpected);

        server.expect(ExpectedCount.once(), requestTo(
                URL_MAIN + "getFxRatesForCurrency?tp=EU&ccy=" + CcyISO4217.USD
                        + "&dtFrom=" + LocalDate.now().minusYears(1)
                        + "&dtTo=" + LocalDate.now()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedReturnBody, MediaType.APPLICATION_XML));

        mainController.getCurrencyHistory(CcyISO4217.USD, model);

        server.verify();
        List<FxRateHandling> actualCurrencyRatesList = (List<FxRateHandling>) model.getAttribute("currencyRatesList");

        assertThat(actualCurrencyRatesList).hasSize(5);
        assertThat(actualCurrencyRatesList.get(0).getTp())
                .isEqualTo(FxRateTypeHandling.EU);
        assertThat(actualCurrencyRatesList.get(0).getCcyAmt().get(1).getCcy())
                .isEqualTo(CcyISO4217.USD);
    }

    @Test
    void getCurrencyHistoryCustom() throws DatatypeConfigurationException, JsonProcessingException {
        FxRatesHandling fxRatesExpected = createFxRatesWithListElements(10);
        String expectedReturnBody = xmlMapper.writeValueAsString(fxRatesExpected);
        DateHistoryTemplate urlInput = new DateHistoryTemplate(CcyISO4217.USD, LocalDate.parse("2020-01-01"), LocalDate.parse("2020-02-10"));

        server.expect(ExpectedCount.once(), requestTo(
                URL_MAIN + "getFxRatesForCurrency?tp=EU&ccy=" + urlInput.getCcy()
                        + "&dtFrom=" + urlInput.getStartDate()
                        + "&dtTo=" + urlInput.getEndDate()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedReturnBody, MediaType.APPLICATION_XML));
        DateHistoryTemplate dateTemplate = new DateHistoryTemplate(CcyISO4217.USD, urlInput.getStartDate(), urlInput.getEndDate());

        mainController.getCurrencyHistoryCustom(urlInput.getCcy(), dateTemplate, bindingResult, model);

        server.verify();
        List<FxRateHandling> actualCurrencyRatesList = (List<FxRateHandling>) model.getAttribute("currencyRatesList");

        assertThat(actualCurrencyRatesList).hasSize(10);
        assertThat(actualCurrencyRatesList.get(0).getTp())
                .isEqualTo(FxRateTypeHandling.EU);
        assertThat(actualCurrencyRatesList.get(0).getCcyAmt().get(1).getCcy())
                .isEqualTo(CcyISO4217.USD);
    }

    @Test
    void getCurrencyHistoryCustomWithErrors() throws DatatypeConfigurationException, JsonProcessingException {
        FxRatesHandling fxRatesExpected = createFxRatesWithListElements(10);
        String expectedReturnBody = xmlMapper.writeValueAsString(fxRatesExpected);

        DateHistoryTemplate urlInput = createDateHistoryTemplate();
        server.expect(ExpectedCount.once(), requestTo(
                URL_MAIN + "getFxRatesForCurrency?tp=EU&ccy=" + urlInput.getCcy()
                        + "&dtFrom=" + urlInput.getStartDate()
                        + "&dtTo=" + urlInput.getEndDate()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedReturnBody, MediaType.APPLICATION_XML));

        DateHistoryTemplate dateTemplate = createDateHistoryTemplate(urlInput);

        bindingResult.addError(new FieldError("dateInputTemplate",
                "endDate", "Bad end date"));
        bindingResult.addError(new FieldError("dateInputTemplate",
                "startDate", "Bad start date"));

        mainController.getCurrencyHistoryCustom(dateTemplate.getCcy(), dateTemplate, bindingResult, model);

        server.verify();
        List<FxRateHandling> actualCurrencyRatesList = (List<FxRateHandling>) model.getAttribute("currencyRatesList");

        assertThat(actualCurrencyRatesList).hasSize(10);
        assertThat(actualCurrencyRatesList.get(0).getTp())
                .isEqualTo(FxRateTypeHandling.EU);
        assertThat(actualCurrencyRatesList.get(0).getCcyAmt().get(1).getCcy())
                .isEqualTo(CcyISO4217.USD);
    }

    @Test
    void convertCurrencyGet() throws DatatypeConfigurationException, JsonProcessingException {
        FxRatesHandling fxRatesExpected = createFxRatesWithListElements(5);
        String expectedReturnBody = xmlMapper.writeValueAsString(fxRatesExpected);

        server.expect(ExpectedCount.once(), requestTo(
                URL_MAIN + URL_CURRENCY_CURRENT))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedReturnBody, MediaType.APPLICATION_XML));

        mainController.convertCurrencyGet(model);

        server.verify();
        List<FxRateHandling> actualCurrencyList = (List<FxRateHandling>) model.getAttribute("currencyNameList");
        assertThat(actualCurrencyList).hasSize(5);
    }

    @Test
    void convertCurrencyPost() throws DatatypeConfigurationException, JsonProcessingException {
        CcyComparator expectedComparator = createCcyComparator2(BigDecimal.ONE, BigDecimal.valueOf(1.2), BigDecimal.TEN);
        CcyComparator actualComparatorTemplate = createCcyComparator(BigDecimal.TEN);
        FxRatesHandling fxRatesExpected = createFxRatesWithListElements(5);

        String expectedReturnBody = xmlMapper.writeValueAsString(fxRatesExpected);
        server.expect(ExpectedCount.once(), requestTo(
                URL_MAIN + URL_CURRENCY_CURRENT))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedReturnBody, MediaType.APPLICATION_XML));

        mainController.convertCurrencyPost(actualComparatorTemplate, bindingResult, model);

        server.verify();
        List<FxRateHandling> actualCurrencyList = (List<FxRateHandling>) model.getAttribute("currencyNameList");
        CcyComparator actualComparator = (CcyComparator) model.getAttribute("ccyComparatorAns");

        assertThat((actualComparator.getCurrency1().getCcy()))
                .isEqualTo(expectedComparator.getCurrency1().getCcy());
        assertThat((actualComparator.getCurrency2().getCcy()))
                .isEqualTo(expectedComparator.getCurrency2().getCcy());
        assertThat((actualComparator.getCurrency1().getAmt()))
                .isGreaterThan(BigDecimal.ZERO);
        assertThat((actualComparator.getCurrency2().getAmt()))
                .isGreaterThan(BigDecimal.ZERO);
        assertThat(actualCurrencyList).hasSize(5);

    }

    @Test
    void convertCurrencyPostWithErrors() throws DatatypeConfigurationException, JsonProcessingException {
        CcyComparator expectedComparator = new CcyComparator();
        CcyComparator actualComparatorTemplate = createCcyComparator(BigDecimal.TEN);

        FxRatesHandling fxRatesExpected = createFxRatesWithListElements(5);
        String expectedReturnBody = xmlMapper.writeValueAsString(fxRatesExpected);

        server.expect(ExpectedCount.once(), requestTo(
                URL_MAIN + URL_CURRENCY_CURRENT))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedReturnBody, MediaType.APPLICATION_XML));
        bindingResult.addError(new FieldError("dateInputTemplate",
                "endDate", "Bad end date"));
        bindingResult.addError(new FieldError("dateInputTemplate",
                "startDate", "Bad start date"));

        mainController.convertCurrencyPost(actualComparatorTemplate, bindingResult, model);

        server.verify();
        List<FxRateHandling> actualCurrencyList = (List<FxRateHandling>) model.getAttribute("currencyNameList");
        CcyComparator actualComparator = (CcyComparator) model.getAttribute("ccyComparatorAns");
        assertThat((actualComparator)).isEqualTo(expectedComparator);
        assertThat(actualCurrencyList).hasSize(5);

    }

    private DateHistoryTemplate createDateHistoryTemplate(DateHistoryTemplate urlInput) {
        DateHistoryTemplate dateTemplate = new DateHistoryTemplate();
        dateTemplate.setStartDate(urlInput.getStartDate());
        dateTemplate.setEndDate(urlInput.getEndDate());
        dateTemplate.setCcy(urlInput.getCcy());
        return dateTemplate;
    }

    private DateHistoryTemplate createDateHistoryTemplate() {
        DateHistoryTemplate urlInput = new DateHistoryTemplate();
        urlInput.setCcy(CcyISO4217.USD);
        urlInput.setEndDate(LocalDate.now());
        urlInput.setStartDate(urlInput.getEndDate().minusYears(1));
        return urlInput;
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

    private CcyComparator createCcyComparator2(BigDecimal amt1, BigDecimal amt2, BigDecimal amount) {
        CcyComparator expectedComparator = new CcyComparator();
        CcyAmtHandling ccyAmt1 = new CcyAmtHandling();
        ccyAmt1.setCcy(CcyISO4217.EUR);
        ccyAmt1.setAmt(amt1);
        CcyAmtHandling ccyAmt2 = new CcyAmtHandling();
        ccyAmt2.setCcy(CcyISO4217.EUR);
        ccyAmt2.setAmt(amt2);
        expectedComparator.setAmount(amount);
        expectedComparator.setCurrency1(ccyAmt1);
        expectedComparator.setCurrency2(ccyAmt2);
        return expectedComparator;
    }

    private CcyComparator createCcyComparator(BigDecimal amount) {
        return createCcyComparator2(null, null, amount);
    }
}