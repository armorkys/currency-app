package testexample.currencyapiexample.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lt.lb.webservices.fxrates.CcyAmtHandling;
import lt.lb.webservices.fxrates.CcyISO4217;
import testexample.currencyapiexample.model.CcyComparator;
import testexample.currencyapiexample.model.CurrencyRatesHandler;
import testexample.currencyapiexample.repository.FxStorage;

@Service
public class CcyComparatorService {

	@Autowired
	private FxStorage fxStorage;

	public CcyComparator getComparatorValuesForCcy(CcyComparator currencyComparator) {
		if (currencyComparator.getCurrency1() == null || currencyComparator.getCurrency2() == null
				|| currencyComparator.getAmount() == null)
			return new CcyComparator();

		setCurrencyValues(currencyComparator.getCurrency1().getCcy(), currencyComparator.getCurrency1());
		setCurrencyValues(currencyComparator.getCurrency2().getCcy(), currencyComparator.getCurrency2());
		return calculateRate(currencyComparator);
	}

	private void setCurrencyValues(CcyISO4217 ccy, CcyAmtHandling ccyAmt) {
		if (ccy == CcyISO4217.EUR) {
			ccyAmt.setCcy(ccy);
			ccyAmt.setAmt(new BigDecimal("1"));
		} else {
			CurrencyRatesHandler currencyRatesHandler = fxStorage.findCurrencyByName(ccy);
			ccyAmt.setAmt(currencyRatesHandler.getAmt());
			ccyAmt.setCcy(ccy);
		}
	}

	private CcyComparator calculateRate(CcyComparator currencyComparator) {

		BigDecimal val1 = currencyComparator.getCurrency1().getAmt();
		BigDecimal val2 = currencyComparator.getCurrency2().getAmt();
		currencyComparator.setRatio(val1);

		val2 = val2.divide(val1, 6, RoundingMode.CEILING);
		val1 = val1.divide(val1, 6, RoundingMode.CEILING);

		val1 = val1.multiply(currencyComparator.getAmount());
		val2 = val2.multiply(currencyComparator.getAmount());

		currencyComparator.getCurrency1().setAmt(val1);
		currencyComparator.getCurrency2().setAmt(val2);

		return currencyComparator;
	}

}
