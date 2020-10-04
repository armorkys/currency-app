package testexample.currencyapiexample.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import testexample.currencyapiexample.model.CurrencyRatesHandler;

@Repository
public interface CurrencyRatesHandlerRepository extends CrudRepository<CurrencyRatesHandler, Integer> {
    @Query("SELECT n FROM CurrencyRatesHandler n WHERE n.ccy = ?1")
    CurrencyRatesHandler findByCcy(String ccyRequest);
}
