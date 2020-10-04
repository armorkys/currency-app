package testexample.currencyapiexample.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lt.lb.webservices.fxrates.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static testexample.currencyapiexample.service.MainService.URLMAIN;

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
        FxRateHandling fxRate1 = new FxRateHandling();
        fxRate1.setDt(DatatypeFactory.newInstance().newXMLGregorianCalendar("2020-01-01T00:00:00.000Z"));
        fxRate1.setTp(FxRateTypeHandling.EU);
        FxRateHandling fxRate = fxRate1;
        expectedFxRates.getFxRate().add(fxRate);

        String expectedReturnBody = xmlMapper.writeValueAsString(expectedFxRates);

        String ccy = CcyISO4217.EUR.value();
        String startDate = "2020-01-01";
        String endDate = "2020-02-01";

        server.expect(ExpectedCount.once(), requestTo(URLMAIN + "getFxRatesForCurrency?tp=EU&ccy=" + ccy + "&dtFrom=" + startDate + "&dtTo=" + endDate))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedReturnBody, MediaType.APPLICATION_XML));

        FxRatesHandling actualFxRates = mainService.getCurrencyHistoryBase(ccy, startDate, endDate);

        server.verify();
        assertThat(actualFxRates.getFxRate()).hasSize(1);
        assertThat(actualFxRates.getFxRate().get(0).getCcyAmt()).isEqualTo(expectedFxRates.getFxRate().get(0).getCcyAmt());
        assertThat(actualFxRates.getFxRate().get(0).getDt()).isEqualTo(expectedFxRates.getFxRate().get(0).getDt());
        assertThat(actualFxRates.getFxRate().get(0).getTp()).isEqualTo(expectedFxRates.getFxRate().get(0).getTp());
        assertThat(actualFxRates.getOprlErr()).isEqualTo(expectedFxRates.getOprlErr());
    }

    @Test
    @Disabled("fix me")
    public void whenHistoricalRatesQueried_LBReturns500_ListOfRatesIsEmpty() throws DatatypeConfigurationException, JsonProcessingException {
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

        String ccy = CcyISO4217.EUR.value();
        String startDate = "2020-01-01";
        String endDate = "2020-02-01";

        server.expect(ExpectedCount.once(), requestTo(URLMAIN + "getFxRatesForCurrency?tp=EU&ccy=" + ccy + "&dtFrom=" + startDate + "&dtTo=" + endDate))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError().body(expectedReturnBody));

        FxRatesHandling actualFxRates = mainService.getCurrencyHistoryBase(ccy, startDate, endDate);

        server.verify();
        assertThat(actualFxRates.getFxRate()).isEmpty();
        assertThat(actualFxRates.getOprlErr()).isEqualTo(expectedFxRates.getOprlErr());
    }
}