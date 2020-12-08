package testexample.currencyapiexample.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import lt.lb.webservices.fxrates.CcyISO4217;
import lt.lb.webservices.fxrates.FxRateHandling;
import lt.lb.webservices.fxrates.FxRatesHandling;
import testexample.currencyapiexample.repository.FxStorage;

@Service
public class FxRatesService {

	private FxClient fxClient;
	
	private FxStorage fxStorage;
		
	@Autowired
	public FxRatesService(FxClient fxClient, FxStorage fxStorage) {
		this.fxClient = fxClient;
		this.fxStorage = fxStorage;
		FxRatesHandling fxRates = fxClient.requestRatesListFromAPI();
		fxStorage.initializeDb(fxRates);
	}
	
	public FxRatesHandling getCurrentCurrencyRates(){
		return fxStorage.getCurrentCurrencyRates();
		
	}

	public FxRatesHandling getCurrencyHistory(CcyISO4217 ccy, LocalDate startDate, LocalDate endDate) {
		return fxClient.getCurrencyHistory(ccy, startDate, endDate);
	}


	public List<String> getCurrencyList() {
		FxRatesHandling fxRates = fxStorage.getCurrentCurrencyRates();

		List<String> ccyList = new ArrayList<>();
		for(FxRateHandling fx:fxRates.getFxRate()){
			ccyList.add(fx.getCcyAmt().get(1).getCcy().toString());
		}
		return ccyList;
	}
}
