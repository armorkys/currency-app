package testexample.currencyapiexample.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import testexample.currencyapiexample.model.CurrencyRatesHandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;


@DataJpaTest
class CurrencyRatesHandlerRepositoryTest {



	@Autowired
	private CurrencyRatesHandlerRepository repository;

	@Test
	void testRepository() {

		BigDecimal bigDecimal = new BigDecimal("1.5");
		bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_EVEN);
		CurrencyRatesHandler expectedHandler = new CurrencyRatesHandler("EUR", bigDecimal, LocalDate.now(), "EU");

		repository.save(expectedHandler);

		CurrencyRatesHandler actualHandler = repository.findByCcy("EUR");

		assertNotNull(repository.getOne(0));
		assertEquals(expectedHandler.getAmt(), actualHandler.getAmt());
		assertEquals(expectedHandler.getCcy(), actualHandler.getCcy());
		assertEquals(expectedHandler.getDt(), actualHandler.getDt());
		assertEquals(expectedHandler.getTp(), actualHandler.getTp());
	}

}
