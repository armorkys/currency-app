package testexample.currencyapiexample.repository;

import java.time.LocalDate;

import javax.xml.datatype.DatatypeConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lt.lb.webservices.fxrates.CcyISO4217;
import lt.lb.webservices.fxrates.FxRateHandling;
import lt.lb.webservices.fxrates.FxRatesHandling;
import testexample.currencyapiexample.model.CurrencyRatesHandler;

@Service
public class FxStorage {

	@Autowired
	private CurrencyRatesHandlerRepository dbRepository;
	
	/** Must be called before getCurrentCurrencyRates **/
	public void initializeDb(FxRatesHandling fxRates) {
		if(fxRates.getOprlErr()==null && fxRates.getFxRate().iterator().hasNext()) {
		giveDataToDatabase(fxRates);
		}
	}

	public boolean updateDB(FxRatesHandling fxRates) {
		if (fxRates.getOprlErr()==null && fxRates.getFxRate().iterator().hasNext()) {
			dbRepository.deleteAll();
			giveDataToDatabase(fxRates);
			return true;
		} else {
			return false;
		}
	}

	private void giveDataToDatabase(FxRatesHandling ans) {
		for (FxRateHandling fxRate : ans.getFxRate()) {
			dbRepository.save(convertFxRateHandlingToCurrencyRatesHandler(fxRate));
		}
	}

	// Converting values for db, BigDecimal remains as it is, others converted to
	// string
	private CurrencyRatesHandler convertFxRateHandlingToCurrencyRatesHandler(FxRateHandling fxRate) {
		return new CurrencyRatesHandler(fxRate.getCcyAmt().get(1).getCcy().toString(),
				fxRate.getCcyAmt().get(1).getAmt(),
				LocalDate.of(fxRate.getDt().getYear(), fxRate.getDt().getMonth(), fxRate.getDt().getDay()),
				fxRate.getTp().toString());
	}

	/** initializeDB must be called before this **/
	public FxRatesHandling getCurrentCurrencyRates(){

		Iterable<CurrencyRatesHandler> iterableList = dbRepository.findAll();
		FxRatesHandling fxRates = new FxRatesHandling();

		if(iterableList!=null) {
			try {
		for (CurrencyRatesHandler currencyRates : iterableList) {
				fxRates.getFxRate().add(CurrencyRatesToFxRates.convertCurrencyRatesHandlerToFxRateHandler(currencyRates));
		}
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		}
		return fxRates;
	}

	public CurrencyRatesHandler findCurrencyByName(CcyISO4217 ccy) {
		return dbRepository.findByCcy(ccy.toString());
	}

}
