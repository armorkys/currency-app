package testexample.currencyapiexample.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLEngineResult.Status;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import lt.lb.webservices.fxrates.CcyAmtHandling;
import lt.lb.webservices.fxrates.CcyISO4217;
import lt.lb.webservices.fxrates.ErrorCode;
import lt.lb.webservices.fxrates.FxRateHandling;
import lt.lb.webservices.fxrates.FxRateTypeHandling;
import lt.lb.webservices.fxrates.FxRatesHandling;
import lt.lb.webservices.fxrates.OprlErrHandling;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import testexample.currencyapiexample.component.RestTemplateResponseErrorHandler;
import testexample.currencyapiexample.model.CurrencyRatesHandler;
import testexample.currencyapiexample.model.DateHistoryTemplate;
import testexample.currencyapiexample.repository.CurrencyRatesHandlerRepository;
import testexample.currencyapiexample.repository.FxStorage;

@SpringBootTest
@AutoConfigureMockMvc
class FxClientTest {

	private final XmlMapper xmlMapper = new XmlMapper();

	@Autowired
	private FxClient fxClient;

	@Autowired
	private RestTemplate restTemplate;

	private MockRestServiceServer server;

    private ObjectMapper mapper = new ObjectMapper();

	@BeforeEach
	void setup() {
		server = MockRestServiceServer.createServer(restTemplate);
	}

	@Test
	public void WhenRequestingRatesListFromApi() throws DatatypeConfigurationException, JsonProcessingException, URISyntaxException {
		FxRatesHandling expectedFxRates = createFxRatesWithListElements(1);
		String expectedReturnBody = xmlMapper.writeValueAsString(expectedFxRates);
				     
	
		  server.expect(ExpectedCount.once(), requestTo(FxClient.URL_MAIN +
		  FxClient.URL_CURRENCY_CURRENT)) .andExpect(method(HttpMethod.GET))
		  .andRespond(withSuccess(expectedReturnBody, MediaType.APPLICATION_XML));
	  
		  
		  FxRatesHandling actualFxRates = fxClient.requestRatesListFromAPI();
		  server.verify();
		  
		  assertThat(actualFxRates.getFxRate()).hasSize(1);
		  assertThat(actualFxRates.getFxRate().get(0).getDt()).isEqualTo(
		  expectedFxRates.getFxRate().get(0).getDt());
		  assertThat(actualFxRates.getFxRate().get(0).getTp()).isEqualTo(
		  expectedFxRates.getFxRate().get(0).getTp());
		  assertThat(actualFxRates.getOprlErr()).isEqualTo(expectedFxRates.getOprlErr());
	
	}

	@Disabled
	public void whenHistoricalRatesQueried_ListOfRatesRetrieved_DatesBefore2014()
			throws DatatypeConfigurationException, JsonProcessingException {
		FxRatesHandling expectedFxRates = createFxRatesWithListElements(1);

		String expectedReturnBody = xmlMapper.writeValueAsString(expectedFxRates);
		DateHistoryTemplate urlData = new DateHistoryTemplate(CcyISO4217.EUR, LocalDate.parse("2014-09-30"),
				LocalDate.parse("2014-09-30"));

		server.expect(ExpectedCount.once(),
				requestTo(FxClient.URL_MAIN + "getFxRatesForCurrency?tp=EU&ccy=" + urlData.getCcy() + "&dtFrom="
						+ urlData.getStartDate() + "&dtTo=" + urlData.getEndDate()))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(expectedReturnBody, MediaType.APPLICATION_XML));

		FxRatesHandling actualFxRates = fxRatesService.getCurrencyHistory(urlData.getCcy(),
				LocalDate.parse("2011-01-01"), LocalDate.parse("2012-01-01"));
		server.verify();

		assertThat(actualFxRates.getFxRate()).hasSize(1);
		assertThat(actualFxRates.getFxRate().get(0).getDt()).isEqualTo(expectedFxRates.getFxRate().get(0).getDt());
		assertThat(actualFxRates.getFxRate().get(0).getTp()).isEqualTo(expectedFxRates.getFxRate().get(0).getTp());
		assertThat(actualFxRates.getOprlErr()).isEqualTo(expectedFxRates.getOprlErr());
	}

	@Disabled
	public void whenHistoricalRatesQueried_LBReturns500_ListOfFxRatesIsEmpty()
			throws DatatypeConfigurationException, JsonProcessingException {
		FxRatesHandling expectedFxRates = createFxRatesEmptyWithErrors(1, "500", "Expected this error in test");
		String expectedReturnBody = xmlMapper.writeValueAsString(expectedFxRates);

		DateHistoryTemplate urlData = new DateHistoryTemplate(CcyISO4217.EUR, LocalDate.parse("2020-01-01"),
				LocalDate.parse("2020-02-01"));

		server.expect(ExpectedCount.once(),
				requestTo(FxClient.URL_MAIN + "getFxRatesForCurrency?tp=EU&ccy=" + urlData.getCcy() + "&dtFrom="
						+ urlData.getStartDate() + "&dtTo=" + urlData.getEndDate()))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withServerError().body(expectedReturnBody).contentType(MediaType.APPLICATION_XML));

		FxRatesHandling actualFxRates = fxRatesService.getCurrencyHistory(urlData.getCcy(), urlData.getStartDate(),
				urlData.getEndDate());

		server.verify();
		assertThat(actualFxRates.getFxRate()).isEmpty();
		assertThat(actualFxRates.getOprlErr().getErr().getPrtry())
				.isEqualTo(expectedFxRates.getOprlErr().getErr().getPrtry());
	}

	@Disabled
	public void whenCurrentRatesQueried_LBReturns500_ListOfFxRatesIsEmpty()
			throws DatatypeConfigurationException, JsonProcessingException {
		FxRatesHandling expectedFxRates = createFxRatesEmptyWithErrors(1, "500", "Expecting this error in test");

		String expectedReturnBody = xmlMapper.writeValueAsString(expectedFxRates);

		server.expect(ExpectedCount.once(), requestTo(FxClient.URL_MAIN + FxClient.URL_CURRENCY_CURRENT))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withServerError().body(expectedReturnBody).contentType(MediaType.APPLICATION_XML));

		List<FxRateHandling> actualFxRates = fxRatesService.getCurrentCurrencyRates();
		server.verify();

		assertThat(actualFxRates).isEmpty();

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
			FxRateHandling fxRate = addDataToFxRateHandling("2020-01-01", FxRateTypeHandling.EU);
			expectedFxRates.getFxRate().add(fxRate);
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
