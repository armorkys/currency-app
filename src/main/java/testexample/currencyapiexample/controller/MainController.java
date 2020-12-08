package testexample.currencyapiexample.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lt.lb.webservices.fxrates.CcyISO4217;
import lt.lb.webservices.fxrates.FxRateHandling;
import lt.lb.webservices.fxrates.FxRatesHandling;
import testexample.currencyapiexample.service.FxRatesService;

@RestController
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);


    @Autowired
    private FxRatesService fxRatesService;

    @GetMapping("/getCurrent")
    public FxRatesHandling getCurrentRatesList() {
        logger.info("\nCalled getCurrent\n");
        FxRatesHandling fxRates = fxRatesService.getCurrentCurrencyRates();
        //showReturn(fxRates);
        return fxRates;
    }

    @GetMapping("/getCurrencyHistory/{ccy}")
    public FxRatesHandling getCurrencyHistory(
            @PathVariable(value = "ccy") String ccy) {
        CcyISO4217 currencyName = CcyISO4217.fromValue(ccy);
        LocalDate dateTo = LocalDate.now();
        LocalDate dateFrom = dateTo.minusYears(1);

        logger.info("\nCalled getCurrentHistory for " + ccy +
                " Date from - " + dateFrom +
                " Date to - " + dateTo);

        FxRatesHandling fxRates = fxRatesService.getCurrencyHistory(currencyName, dateFrom, dateTo);
        // showReturn(fxRates);
        return fxRates;
    }

    @GetMapping("/getCurrencyHistoryCustom/{ccy}+from={dateFrom}+dateTo{dateTo}")
    public FxRatesHandling getCurrencyHistoryCustom(
            @PathVariable(value = "ccy") String ccy,
            @PathVariable(value = "dateFrom") String startDate,
            @PathVariable(value = "dateTo") String endDate) {
        logger.info("\nCalled getCurrentHistory for " + ccy +
                " Date from - " + startDate +
                " Date to - " + endDate);
        CcyISO4217 currencyName = CcyISO4217.fromValue(ccy);
        LocalDate dateFrom = LocalDate.parse(startDate);
        LocalDate dateTo = LocalDate.parse(endDate);
        FxRatesHandling fxRates = fxRatesService.getCurrencyHistory(currencyName, dateFrom, dateTo);
       // showReturn(fxRates);
        return fxRates;
    }

    @GetMapping("/getCurrencyList")
    public List<String> getCurrencyList() {
        logger.info("Getting currency list");
        List<String> curList = fxRatesService.getCurrencyList();
        logger.info("Currency name list size - " + curList);
        return curList;
    }

    private void showReturn(FxRatesHandling fxRates) {
        logger.info("\nSize - " + fxRates.getFxRate().size());
        for (FxRateHandling f : fxRates.getFxRate()) {
            logger.info("Name - " + f.getCcyAmt().get(1).getCcy() + " Value - " + f.getCcyAmt().get(1).getAmt() + " Date - " + f.getDt());
        }
    }

}
