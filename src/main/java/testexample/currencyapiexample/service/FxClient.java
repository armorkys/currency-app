package testexample.currencyapiexample.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import lt.lb.webservices.fxrates.CcyISO4217;
import lt.lb.webservices.fxrates.ErrorCode;
import lt.lb.webservices.fxrates.FxRatesHandling;
import lt.lb.webservices.fxrates.OprlErrHandling;
import testexample.currencyapiexample.component.RestTemplateResponseErrorHandler;

@Service
public class FxClient {

	public static final String URL_MAIN = "http://www.lb.lt/webservices/FxRates/FxRates.asmx/";
	public static final String URL_CURRENCY_HISTORY = "getFxRatesForCurrency?tp=EU&ccy={ccy}&dtFrom={startDate}&dtTo={endDate}";
	public static final String URL_CURRENCY_CURRENT = "getCurrentFxRates?tp=EU";

	private RestTemplate restTemplate;

	@Autowired
	public FxClient(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder
				.errorHandler(new RestTemplateResponseErrorHandler())
				.build();
	}

	public FxRatesHandling requestRatesListFromAPI() {
		FxRatesHandling fxRatesReturn;
		try {
			ResponseEntity<FxRatesHandling> returnEntity = restTemplate.getForEntity(URL_MAIN + URL_CURRENCY_CURRENT,
					FxRatesHandling.class);
			fxRatesReturn = returnEntity.getBody();
		} catch (HttpStatusCodeException e) {
			fxRatesReturn = new FxRatesHandling();
			OprlErrHandling error = new OprlErrHandling();
			error.setDesc("Http status code exception");
			ErrorCode errorCode = new ErrorCode();
			errorCode.setPrtry("500");
			error.setErr(errorCode);
			fxRatesReturn.setOprlErr(error);
		}
		return fxRatesReturn;
	}

	public FxRatesHandling getCurrencyHistory(CcyISO4217 ccy, LocalDate startDate, LocalDate endDate) {
		FxRatesHandling fxRatesReturn;
		LocalDate minimumDate = LocalDate.parse("2014-09-30");
		if (startDate.isBefore(minimumDate))
			startDate = minimumDate;
		if (endDate.isBefore(minimumDate))
			endDate = minimumDate;
		try {
			ResponseEntity<FxRatesHandling> returnEntity = restTemplate.getForEntity(URL_MAIN + URL_CURRENCY_HISTORY,
					FxRatesHandling.class, ccy, startDate, endDate);
			fxRatesReturn = returnEntity.getBody();
		} catch (HttpStatusCodeException e) {
			fxRatesReturn = new FxRatesHandling();
			OprlErrHandling error = new OprlErrHandling();
			error.setDesc("Http status code exception");
			ErrorCode errorCode = new ErrorCode();
			errorCode.setPrtry("500");
			error.setErr(errorCode);
			fxRatesReturn.setOprlErr(error);
		}
		return fxRatesReturn;
	}

}
