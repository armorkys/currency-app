package testexample.currencyapiexample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import testexample.currencyapiexample.model.CurrencyRatesHandler;

@Repository
public interface CurrencyRatesHandlerRepository extends JpaRepository<CurrencyRatesHandler, Integer> {
	@Query("SELECT n FROM CurrencyRatesHandler n WHERE n.ccy = ?1")
	CurrencyRatesHandler findByCcy(String ccyRequest);
}
