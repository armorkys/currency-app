package testexample.currencyapiexample.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import lt.lb.webservices.fxrates.CcyAmtHandling;
import lt.lb.webservices.fxrates.CcyISO4217;
import testexample.currencyapiexample.model.CcyComparator;
import testexample.currencyapiexample.model.CurrencyRatesHandler;
import testexample.currencyapiexample.repository.CurrencyRatesHandlerRepository;

@SpringBootTest
@AutoConfigureMockMvc
class CcyComparatorServiceTest {

	@Autowired
	@MockBean
	CurrencyRatesHandlerRepository database;

	@Autowired
	CcyComparatorService comparatorService;

	@AfterEach
	void cleanup() {
		database.deleteAll();
	}

	@Test
	public void getComparatorValuesForCcy_CcyComparatorEmpty() {
		CcyComparator expectedComparator = new CcyComparator();
		CcyComparator actualComparator = comparatorService.getComparatorValuesForCcy(expectedComparator);
		assertThat(actualComparator).isEqualTo(expectedComparator);
	}

	@Test
	public void getComparatorValuesForCcy_CcyComparatorNormalValues() {
		CcyComparator expectedComparator = createCcyComparator3(BigDecimal.TEN, CcyISO4217.USD, CcyISO4217.EUR);

		List<String> currencyNames = Arrays.asList("GBP", "USD", "AUR", "EUR");
		List<CurrencyRatesHandler> currencyRatesList = createCurrencyRatesList(4, currencyNames);

		Mockito.when(database.findByCcy("USD")).thenReturn(currencyRatesList.get(1));
		Mockito.when(database.findByCcy("EUR")).thenReturn(currencyRatesList.get(3));

		CcyComparator actualComparator = comparatorService.getComparatorValuesForCcy(expectedComparator);

		assertThat(actualComparator.getAmount()).isEqualTo(expectedComparator.getAmount());
		assertThat(actualComparator.getCurrency1().getCcy()).isEqualTo(expectedComparator.getCurrency1().getCcy());
		assertThat(actualComparator.getCurrency2().getCcy()).isEqualTo(expectedComparator.getCurrency2().getCcy());
		assertThat(actualComparator.getCurrency1().getAmt()).isGreaterThan(BigDecimal.ZERO);
		assertThat(actualComparator.getCurrency2().getAmt()).isGreaterThan(BigDecimal.ZERO);
	}

	private List<CurrencyRatesHandler> createCurrencyRatesList(int amount, List<String> currencyNames) {
		List<CurrencyRatesHandler> currencyRatesList = new ArrayList<>();
		for (int step = 0; step < amount; step++) {
			currencyRatesList.add(
					new CurrencyRatesHandler(step, "EUR", new BigDecimal(step), LocalDate.parse("2020-01-01"), "EU"));
		}
		return currencyRatesList;
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

	private CcyComparator createCcyComparator3(BigDecimal amount, CcyISO4217 ccy1, CcyISO4217 ccy2) {
		CcyComparator ccyComparator = createCcyComparator(amount);
		ccyComparator.getCurrency1().setCcy(ccy1);
		ccyComparator.getCurrency2().setCcy(ccy2);
		return ccyComparator;
	}
}