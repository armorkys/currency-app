package testexample.currencyapiexample.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lt.lb.webservices.fxrates.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.client.RestTemplate;
import testexample.currencyapiexample.model.CcyComparator;
import testexample.currencyapiexample.model.DateHistoryTemplate;
import testexample.currencyapiexample.repository.CurrencyRatesHandlerRepository;
import testexample.currencyapiexample.service.MainService;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.beans.PropertyEditor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

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
    @Autowired
    private MainService mainService;

    @AfterEach
    void cleanup() {
        database.deleteAll();
    }

    @Test
    void getCurrentRatesList() throws DatatypeConfigurationException, JsonProcessingException {
        Model model = new TestModel();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        FxRatesHandling fxRatesExpected = createFxRatesWithListElements(2);
        String expectedReturnBody = xmlMapper.writeValueAsString(fxRatesExpected);
        server.expect(ExpectedCount.once(), requestTo(
                URL_MAIN + URL_CURRENCY_CURRENT))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedReturnBody, MediaType.APPLICATION_XML));
        mainController.getCurrentRatesList(model);
        server.verify();
        List<FxRateHandling> currencyList = (List<FxRateHandling>) model.getAttribute("currencyList");
        Assertions.assertThat(
                currencyList).hasSize(2);
    }


    @Test
    void getCurrencyHistory() throws DatatypeConfigurationException, JsonProcessingException {
        Model model = new TestModel();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        FxRatesHandling fxRatesExpected = createFxRatesWithListElements(5);
        String expectedReturnBody = xmlMapper.writeValueAsString(fxRatesExpected);
        DateHistoryTemplate urlInput = new DateHistoryTemplate();
        urlInput.setCcy(CcyISO4217.USD);
        urlInput.setEndDate(LocalDate.now());
        urlInput.setStartDate(urlInput.getEndDate().minusYears(1));
        server.expect(ExpectedCount.once(), requestTo(
                URL_MAIN + "getFxRatesForCurrency?tp=EU&ccy=" + urlInput.getCcy()
                        + "&dtFrom=" + urlInput.getStartDate()
                        + "&dtTo=" + urlInput.getEndDate()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedReturnBody, MediaType.APPLICATION_XML));
        mainController.getCurrencyHistory(CcyISO4217.USD, model);
        server.verify();
        List<FxRateHandling> currencyRatesList = (List<FxRateHandling>) model.getAttribute("currencyRatesList");
        Assertions.assertThat(
                currencyRatesList).hasSize(5);
        Assertions.assertThat(
                currencyRatesList.get(0).getTp()).isEqualTo(FxRateTypeHandling.EU);
        Assertions.assertThat(
                currencyRatesList.get(0).getCcyAmt().get(1).getCcy()).isEqualTo(CcyISO4217.USD);
    }

    @Test
    void getCurrencyHistoryCustom() throws DatatypeConfigurationException, JsonProcessingException {
        Model model = new TestModel();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
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
        BindingResult bindingResult = new TestBindingResult();
        mainController.getCurrencyHistoryCustom(urlInput.getCcy(), dateTemplate, bindingResult, model);
        server.verify();
        List<FxRateHandling> currencyRatesList = (List<FxRateHandling>) model.getAttribute("currencyRatesList");
        Assertions.assertThat(
                currencyRatesList).hasSize(10);
        Assertions.assertThat(
                currencyRatesList.get(0).getTp()).isEqualTo(FxRateTypeHandling.EU);
        Assertions.assertThat(
                currencyRatesList.get(0).getCcyAmt().get(1).getCcy()).isEqualTo(CcyISO4217.USD);
    }

    @Test
    void getCurrencyHistoryCustomWithErrors() throws DatatypeConfigurationException, JsonProcessingException {
        Model model = new TestModel();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        FxRatesHandling fxRatesExpected = createFxRatesWithListElements(10);
        String expectedReturnBody = xmlMapper.writeValueAsString(fxRatesExpected);
        DateHistoryTemplate urlInput = new DateHistoryTemplate();
        urlInput.setCcy(CcyISO4217.USD);
        urlInput.setEndDate(LocalDate.now());
        urlInput.setStartDate(urlInput.getEndDate().minusYears(1));
        server.expect(ExpectedCount.once(), requestTo(
                URL_MAIN + "getFxRatesForCurrency?tp=EU&ccy=" + urlInput.getCcy()
                        + "&dtFrom=" + urlInput.getStartDate()
                        + "&dtTo=" + urlInput.getEndDate()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedReturnBody, MediaType.APPLICATION_XML));

        DateHistoryTemplate dateTemplate = new DateHistoryTemplate();
        dateTemplate.setStartDate(urlInput.getStartDate());
        dateTemplate.setEndDate(urlInput.getEndDate());
        dateTemplate.setCcy(urlInput.getCcy());
        BindingResult bindingResult = new TestBindingResult();
        bindingResult.addError(new FieldError("dateInputTemplate",
                "endDate", "Bad end date"));
        bindingResult.addError(new FieldError("dateInputTemplate",
                "startDate", "Bad start date"));
        mainController.getCurrencyHistoryCustom(dateTemplate.getCcy(), dateTemplate, bindingResult, model);
        server.verify();
        List<FxRateHandling> currencyRatesList = (List<FxRateHandling>) model.getAttribute("currencyRatesList");
        Assertions.assertThat(
                currencyRatesList).hasSize(10);
        Assertions.assertThat(
                currencyRatesList.get(0).getTp()).isEqualTo(FxRateTypeHandling.EU);
        Assertions.assertThat(
                currencyRatesList.get(0).getCcyAmt().get(1).getCcy()).isEqualTo(CcyISO4217.USD);
    }

    @Test
    void convertCurrencyGet() throws DatatypeConfigurationException, JsonProcessingException {
        Model model = new TestModel();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        FxRatesHandling fxRatesExpected = createFxRatesWithListElements(5);
        String expectedReturnBody = xmlMapper.writeValueAsString(fxRatesExpected);
        server.expect(ExpectedCount.once(), requestTo(
                URL_MAIN + URL_CURRENCY_CURRENT))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedReturnBody, MediaType.APPLICATION_XML));
        mainController.convertCurrencyGet(model);
        server.verify();
        List<FxRateHandling> currencyList = (List<FxRateHandling>) model.getAttribute("currencyNameList");
        Assertions.assertThat(
                currencyList).hasSize(5);
    }

    @Test
    void convertCurrencyPost() throws DatatypeConfigurationException, JsonProcessingException {
        Model model = new TestModel();
        CcyComparator expectedComparator = new CcyComparator();
        CcyAmtHandling ccyAmt1 = new CcyAmtHandling();
        ccyAmt1.setCcy(CcyISO4217.EUR);
        ccyAmt1.setAmt(new BigDecimal("1"));
        CcyAmtHandling ccyAmt2 = new CcyAmtHandling();
        ccyAmt2.setCcy(CcyISO4217.EUR);
        ccyAmt2.setAmt(new BigDecimal("1.2"));
        expectedComparator.setAmount(new BigDecimal("10"));
        expectedComparator.setCurrency1(ccyAmt1);
        expectedComparator.setCurrency2(ccyAmt2);
        BindingResult bindingResult = new TestBindingResult();
        CcyComparator actualComparatorTemplate = new CcyComparator();
        CcyAmtHandling ccyAmtActual1 = new CcyAmtHandling();
        ccyAmtActual1.setCcy(CcyISO4217.EUR);
        CcyAmtHandling ccyAmtActual2 = new CcyAmtHandling();
        ccyAmtActual2.setCcy(CcyISO4217.EUR);
        actualComparatorTemplate.setAmount(new BigDecimal("10"));
        actualComparatorTemplate.setCurrency1(ccyAmtActual1);
        actualComparatorTemplate.setCurrency2(ccyAmtActual2);
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        FxRatesHandling fxRatesExpected = createFxRatesWithListElements(5);
        String expectedReturnBody = xmlMapper.writeValueAsString(fxRatesExpected);
        server.expect(ExpectedCount.once(), requestTo(
                URL_MAIN + URL_CURRENCY_CURRENT))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedReturnBody, MediaType.APPLICATION_XML));
        mainController.convertCurrencyPost(actualComparatorTemplate, bindingResult, model);
        server.verify();
        List<FxRateHandling> currencyList = (List<FxRateHandling>) model.getAttribute("currencyNameList");
        CcyComparator actualComparator = (CcyComparator) model.getAttribute("ccyComparatorAns");
        Assertions.assertThat(
                (actualComparator.getCurrency1().getCcy())).isEqualTo(expectedComparator.getCurrency1().getCcy());
        Assertions.assertThat(
                (actualComparator.getCurrency2().getCcy())).isEqualTo(expectedComparator.getCurrency2().getCcy());
        Assertions.assertThat(
                (actualComparator.getCurrency1().getAmt())).isGreaterThan(new BigDecimal("0"));
        Assertions.assertThat(
                (actualComparator.getCurrency2().getAmt())).isGreaterThan(new BigDecimal("0"));
        Assertions.assertThat(
                currencyList).hasSize(5);

    }

    @Test
    void convertCurrencyPostWithErrors() throws DatatypeConfigurationException, JsonProcessingException {
        Model model = new TestModel();
        CcyComparator expectedComparator = new CcyComparator();

        CcyComparator actualComparatorTemplate = new CcyComparator();
        CcyAmtHandling ccyAmtActual1 = new CcyAmtHandling();
        ccyAmtActual1.setCcy(CcyISO4217.EUR);
        CcyAmtHandling ccyAmtActual2 = new CcyAmtHandling();
        ccyAmtActual2.setCcy(CcyISO4217.EUR);
        actualComparatorTemplate.setAmount(new BigDecimal("10"));
        actualComparatorTemplate.setCurrency1(ccyAmtActual1);
        actualComparatorTemplate.setCurrency2(ccyAmtActual2);

        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        FxRatesHandling fxRatesExpected = createFxRatesWithListElements(5);
        String expectedReturnBody = xmlMapper.writeValueAsString(fxRatesExpected);
        server.expect(ExpectedCount.once(), requestTo(
                URL_MAIN + URL_CURRENCY_CURRENT))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedReturnBody, MediaType.APPLICATION_XML));

        BindingResult bindingResult = new TestBindingResult();
        bindingResult.addError(new FieldError("dateInputTemplate",
                "endDate", "Bad end date"));
        bindingResult.addError(new FieldError("dateInputTemplate",
                "startDate", "Bad start date"));

        mainController.convertCurrencyPost(actualComparatorTemplate, bindingResult, model);
        server.verify();
        List<FxRateHandling> currencyList = (List<FxRateHandling>) model.getAttribute("currencyNameList");
        CcyComparator actualComparator = (CcyComparator) model.getAttribute("ccyComparatorAns");

        Assertions.assertThat(
                (actualComparator)).isEqualTo(expectedComparator);
        Assertions.assertThat(
                currencyList).hasSize(5);

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

    private class TestModel implements Model {
        private Map<String, Object> attributes = new HashMap<>();

        @Override
        public Model addAttribute(String attributeName, Object attributeValue) {
            attributes.put(attributeName, attributeValue);
            return this;
        }

        @Override
        public Model addAttribute(Object attributeValue) {
            return null;
        }

        @Override
        public Model addAllAttributes(Collection<?> attributeValues) {
            return null;
        }

        @Override
        public Model addAllAttributes(Map<String, ?> attributes) {
            return null;
        }

        @Override
        public Model mergeAttributes(Map<String, ?> attributes) {
            return null;
        }

        @Override
        public boolean containsAttribute(String attributeName) {
            return false;
        }

        @Override
        public Object getAttribute(String attributeName) {
            return this.attributes.get(attributeName);
        }

        @Override
        public Map<String, Object> asMap() {
            return null;
        }
    }

    private class TestBindingResult implements BindingResult {
        private List<ObjectError> errors = new ArrayList<>();

        @Override
        public String getObjectName() {
            return null;
        }

        @Override
        public String getNestedPath() {
            return null;
        }

        @Override
        public void setNestedPath(String nestedPath) {

        }

        @Override
        public void pushNestedPath(String subPath) {

        }

        @Override
        public void popNestedPath() throws IllegalStateException {

        }

        @Override
        public void reject(String errorCode) {

        }

        @Override
        public void reject(String errorCode, String defaultMessage) {

        }

        @Override
        public void reject(String errorCode, Object[] errorArgs, String defaultMessage) {

        }

        @Override
        public void rejectValue(String field, String errorCode) {

        }

        @Override
        public void rejectValue(String field, String errorCode, String defaultMessage) {

        }

        @Override
        public void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage) {

        }

        @Override
        public void addAllErrors(Errors errors) {

        }

        @Override
        public boolean hasErrors() {
            return (this.errors.size() > 0);
        }

        @Override
        public int getErrorCount() {
            return 0;
        }

        @Override
        public List<ObjectError> getAllErrors() {
            return null;
        }

        @Override
        public boolean hasGlobalErrors() {
            return false;
        }

        @Override
        public int getGlobalErrorCount() {
            return 0;
        }

        @Override
        public List<ObjectError> getGlobalErrors() {
            return null;
        }

        @Override
        public ObjectError getGlobalError() {
            return null;
        }

        @Override
        public boolean hasFieldErrors() {
            return false;
        }

        @Override
        public int getFieldErrorCount() {
            return 0;
        }

        @Override
        public List<FieldError> getFieldErrors() {
            return null;
        }

        @Override
        public FieldError getFieldError() {
            return null;
        }

        @Override
        public boolean hasFieldErrors(String field) {
            return false;
        }

        @Override
        public int getFieldErrorCount(String field) {
            return 0;
        }

        @Override
        public List<FieldError> getFieldErrors(String field) {
            return null;
        }

        @Override
        public FieldError getFieldError(String field) {
            return null;
        }

        @Override
        public Object getFieldValue(String field) {
            return null;
        }

        @Override
        public Class<?> getFieldType(String field) {
            return null;
        }

        @Override
        public Object getTarget() {
            return null;
        }

        @Override
        public Map<String, Object> getModel() {
            return null;
        }

        @Override
        public Object getRawFieldValue(String field) {
            return null;
        }

        @Override
        public PropertyEditor findEditor(String field, Class<?> valueType) {
            return null;
        }

        @Override
        public PropertyEditorRegistry getPropertyEditorRegistry() {
            return null;
        }

        @Override
        public String[] resolveMessageCodes(String errorCode) {
            return new String[0];
        }

        @Override
        public String[] resolveMessageCodes(String errorCode, String field) {
            return new String[0];
        }

        @Override
        public void addError(ObjectError error) {
            this.errors.add(error);
        }
    }
}