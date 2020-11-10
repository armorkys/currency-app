package testexample.currencyapiexample.repository;

import java.math.BigDecimal;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.springframework.stereotype.Service;

import lt.lb.webservices.fxrates.CcyAmtHandling;
import lt.lb.webservices.fxrates.CcyISO4217;
import lt.lb.webservices.fxrates.FxRateHandling;
import lt.lb.webservices.fxrates.FxRateTypeHandling;
import testexample.currencyapiexample.model.CurrencyRatesHandler;

@Service
public class FxRatesToCurrencyRates {

	// converting values from db to main object handlers
	public static FxRateHandling convertCurrencyRatesHandlerToFxRateHandler(CurrencyRatesHandler currencyRates)
			throws DatatypeConfigurationException {

		FxRateHandling fxRate = new FxRateHandling();
		// Because 1st CcyAmtHandling is always EUR with value of 1
		CcyAmtHandling temp1 = new CcyAmtHandling();

		temp1.setCcy(CcyISO4217.valueOf("EUR"));
		temp1.setAmt(new BigDecimal("1"));

		// Values from DB to 2nd CcyAmtHandling
		CcyAmtHandling temp2 = new CcyAmtHandling();
		temp2.setCcy(CcyISO4217.valueOf(currencyRates.getCcy()));
		temp2.setAmt(currencyRates.getAmt());

		// adding other values
		fxRate.getCcyAmt().add(temp1);
		fxRate.getCcyAmt().add(temp2);
		fxRate.setTp(FxRateTypeHandling.valueOf(currencyRates.getTp()));
		fxRate.setDt(DatatypeFactory.newInstance().newXMLGregorianCalendar(currencyRates.getDt().toString()));
		return fxRate;
	}

}
