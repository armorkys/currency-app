package testexample.currencyapiexample.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.fasterxml.jackson.core.JsonProcessingException;

import lt.lb.webservices.fxrates.CcyAmtHandling;
import lt.lb.webservices.fxrates.CcyISO4217;
import lt.lb.webservices.fxrates.ErrorCode;
import lt.lb.webservices.fxrates.FxRateHandling;
import lt.lb.webservices.fxrates.FxRateTypeHandling;
import lt.lb.webservices.fxrates.FxRatesHandling;
import lt.lb.webservices.fxrates.OprlErrHandling;
import testexample.currencyapiexample.model.CurrencyRatesHandler;

@SpringBootTest
@AutoConfigureMockMvc
class FxStorageTest {

	@MockBean
	CurrencyRatesHandlerRepository database;

	@Autowired
	private FxStorage fxStorage;

	@Test
	public void initializeDbQueried_ListOfFxRatesIsNormalValues()
			throws DatatypeConfigurationException, JsonProcessingException {
		FxRatesHandling expectedFxRates = createFxRatesWithListElements(1);

		CurrencyRatesHandler expectedRates = new CurrencyRatesHandler("USD", new BigDecimal("1.2"),
				LocalDate.of(2020, 01, 01), "EU");

		fxStorage.initializeDb(expectedFxRates);

		Mockito.verify(database, times(1)).save(expectedRates);
	}

	@Test
	public void initializeDbQueried_ListOfFxRatesIsEmpty() throws DatatypeConfigurationException {
		FxRatesHandling expectedFxRates = createFxRatesEmptyWithErrors(1, "500", "Expected this error in test");
		CurrencyRatesHandler expectedRates = new CurrencyRatesHandler("USD", new BigDecimal("1.2"),
				LocalDate.of(2020, 01, 01), "EU");

		fxStorage.initializeDb(expectedFxRates);

		Mockito.verify(database, times(0)).save(expectedRates);
	}

	@Disabled
	public void updateDbQueried_ListOfFxRatesIsNormalValues()
			throws DatatypeConfigurationException, JsonProcessingException {
		FxRatesHandling expectedFxRates = createFxRatesWithListElements(4);
		boolean actualValue = fxStorage.updateDB(expectedFxRates);
		assertTrue(actualValue);
	}

	@Test
	public void updateDBQueried_ListOfFxRatesIsEmpty() throws DatatypeConfigurationException, JsonProcessingException {
		FxRatesHandling expectedFxRates = createFxRatesEmptyWithErrors(1, "500", "Expected this error in test");
		boolean actualValue = fxStorage.updateDB(expectedFxRates);
		assertFalse(actualValue);
	}

	@Test
	public void getCurrentCurrencyRates_Queried_DbHasValues() throws DatatypeConfigurationException {
		FxRatesHandling expectedFxRates = createFxRatesWithListElements(4);

		List<CurrencyRatesHandler> curHandler = Arrays.asList(
				new CurrencyRatesHandler("USD", new BigDecimal("1.2"), LocalDate.of(2020, 01, 01), "EU"),
				new CurrencyRatesHandler("USD", new BigDecimal("1.2"), LocalDate.of(2020, 01, 01), "EU"),
				new CurrencyRatesHandler("USD", new BigDecimal("1.2"), LocalDate.of(2020, 01, 01), "EU"),
				new CurrencyRatesHandler("USD", new BigDecimal("1.2"), LocalDate.of(2020, 01, 01), "EU"));

		Mockito.when(database.findAll()).thenReturn(curHandler);

		FxRatesHandling actualValues = fxStorage.getCurrentCurrencyRates();
		Mockito.verify(database, times(1)).findAll();

		int size = actualValues.getFxRate().size();

		for (int i = 0; i < size; i++) {
			assertEquals(actualValues.getFxRate().get(i).getCcyAmt().get(1).getAmt(),
					expectedFxRates.getFxRate().get(i).getCcyAmt().get(1).getAmt());
			assertEquals(actualValues.getFxRate().get(i).getCcyAmt().get(1).getCcy(),
					expectedFxRates.getFxRate().get(i).getCcyAmt().get(1).getCcy());
			assertEquals(actualValues.getFxRate().get(i).getTp(), expectedFxRates.getFxRate().get(i).getTp());
		}
	}

	@Test
	public void getCurrentCurrencyRates_Queried_DbIsEmpty() throws DatatypeConfigurationException {
		List<CurrencyRatesHandler> curHandler = null;

		Mockito.when(database.findAll()).thenReturn(curHandler);

		FxRatesHandling actualValues = fxStorage.getCurrentCurrencyRates();

		Mockito.verify(database, times(1)).findAll();

		assertTrue(actualValues.getFxRate().size() == 0);
	}

	@Test
	public void findCurrencyByName_Queried_NormalValue() {
		CcyISO4217 expectedCcy = CcyISO4217.USD;

		CurrencyRatesHandler usdRates = new CurrencyRatesHandler("USD", new BigDecimal("1.2"),
				LocalDate.of(2020, 01, 01), "EU");

		Mockito.when(database.findByCcy("USD")).thenReturn(usdRates);

		CurrencyRatesHandler expectedRates = fxStorage.findCurrencyByName(expectedCcy);

		Mockito.verify(database, times(1)).findByCcy(expectedCcy.toString());
		assertEquals(usdRates, expectedRates);
	}

	@Test
	public void findCurrencyByName_Queried_BadValue() {
		CcyISO4217 expectedCcy = CcyISO4217.USD;

		CurrencyRatesHandler usdRates = new CurrencyRatesHandler("XXX", new BigDecimal("1.2"),
				LocalDate.of(2020, 01, 01), "EU");

		Mockito.when(database.findByCcy("XXX")).thenReturn(null);

		CurrencyRatesHandler expectedRates = fxStorage.findCurrencyByName(expectedCcy);
		Mockito.verify(database, times(1)).findByCcy(expectedCcy.toString());
		assertNotEquals(usdRates, expectedRates);
		assertTrue(expectedRates == null);
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
