package testexample.currencyapiexample.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import testexample.currencyapiexample.model.CurrencyRatesHandler;

@Repository
public interface CurrencyRatesHandlerRepository extends CrudRepository<CurrencyRatesHandler, Integer> {
   // CurrencyRatesHandler findByccy();
}
