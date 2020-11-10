package testexample.currencyapiexample.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import lt.lb.webservices.fxrates.CcyAmtHandling;
import lt.lb.webservices.fxrates.CcyISO4217;
import lt.lb.webservices.fxrates.ErrorCode;
import lt.lb.webservices.fxrates.FxRateHandling;
import lt.lb.webservices.fxrates.FxRateTypeHandling;
import lt.lb.webservices.fxrates.FxRatesHandling;
import lt.lb.webservices.fxrates.OprlErrHandling;
import testexample.currencyapiexample.model.CurrencyRatesHandler;
import testexample.currencyapiexample.model.DateHistoryTemplate;


@SpringBootTest
@AutoConfigureMockMvc
class FxRatesServiceTest {

	private final XmlMapper xmlMapper = new XmlMapper();
	
	@Autowired
	private RestTemplate restTemplate;
	
	private FxRatesService fxRatesService;

	private MockRestServiceServer server;
	
	@BeforeEach
	void setup() {
		server = MockRestServiceServer.bindTo(restTemplate).build();
	}
	
	@Disabled
	public void whenHistoricalRatesQueried_ListOfRatesRetrieved()
			throws DatatypeConfigurationException, JsonProcessingException {
		
		FxRatesHandling expectedFxRates = createFxRatesWithListElements(1);
        String expectedReturnBody = xmlMapper.writeValueAsString(expectedFxRates);

		DateHistoryTemplate urlData = new DateHistoryTemplate(CcyISO4217.EUR, LocalDate.parse("2020-01-01"),
				LocalDate.parse("2020-02-01"));

		Mockito
		.when(restTemplate.getForEntity(
              FxClient.URL_MAIN + "getFxRatesForCurrency?tp=EU&ccy=" + urlData.getCcy() +
                      "&dtFrom=" + urlData.getStartDate() + "&dtTo=" + urlData.getEndDate(), FxRatesHandling.class))
		.thenReturn(new ResponseEntity(fxRates, HttpStatus.OK));
		
		FxRatesHandling actualFxRates = fxRatesService.getCurrencyHistory(urlData.getCcy(), urlData.getStartDate(), urlData.getEndDate());

		assertEquals(actualFxRates, expectedFxRates);
		
		
		Mockito.when(fxRatesService.getCurrencyHistory(urlData.getCcy(), urlData.getStartDate(),
				urlData.getEndDate())).thenReturn(expectedFxRates);
		
		Mockito.verify(fxRatesService, atLeastOnce()).getCurrencyHistory(urlData.getCcy(), urlData.getStartDate(),
				urlData.getEndDate());
		}
	
	
}
