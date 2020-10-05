package testexample.currencyapiexample.service;

import lt.lb.webservices.fxrates.*;
import org.assertj.core.util.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import testexample.currencyapiexample.component.RestTemplateResponseErrorHandler;
import testexample.currencyapiexample.model.CcyComparator;
import testexample.currencyapiexample.model.CurrencyRatesHandler;
import testexample.currencyapiexample.repository.CurrencyRatesHandlerRepository;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class MainService {

    @VisibleForTesting
    public static final String URL_MAIN = "http://www.lb.lt/webservices/FxRates/FxRates.asmx/";
    public static final String URL_CURRENCY_HISTORY = "getFxRatesForCurrency?tp=EU&ccy={ccy}&dtFrom={startDate}&dtTo={endDate}";
    public static final String URL_CURRENCY_CURRENT = "getCurrentFxRates?tp=EU";

    @Autowired
    CurrencyRatesHandlerRepository dbRepository;

    @Autowired
    @VisibleForTesting
    RestTemplate restTemplate;

    @Autowired
    public MainService(RestTemplateBuilder restTemplateBuilder){
        RestTemplate restTemplate = restTemplateBuilder
                .errorHandler(new RestTemplateResponseErrorHandler())
                .build();
    }


    public FxRatesHandling requestRatesListFromAPI() {
        try {
            ResponseEntity<FxRatesHandling> returnEntity = restTemplate.getForEntity(URL_MAIN + URL_CURRENCY_CURRENT, FxRatesHandling.class);
            return returnEntity.getBody();
        } catch(HttpStatusCodeException e) {
            FxRatesHandling errorFxRates = new FxRatesHandling();
            OprlErrHandling error = new OprlErrHandling();
            error.setDesc("Http status code exception");
            ErrorCode errorCode = new ErrorCode();
            errorCode.setPrtry("500");
            error.setErr(errorCode);
            errorFxRates.setOprlErr(error);
            return errorFxRates;
        }
    }

    public void updateDB(){
        FxRatesHandling fxRates = requestRatesListFromAPI();
        if (fxRates.getFxRate().iterator().hasNext()) {
            dbRepository.deleteAll();
            giveDataToDatabase(fxRates);
        } else if (!fxRates.getFxRate().iterator().hasNext()) {
            System.out.println("No valid response from Server ");
        }
    }

    public FxRatesHandling getCurrentCurrencyRates() throws DatatypeConfigurationException {
        if (dbIsEmpty()) {
            FxRatesHandling fxRates = requestRatesListFromAPI();
            giveDataToDatabase(fxRates);
            return fxRates;
        } else {
            return loadFromDB();
        }
    }

    private boolean dbIsEmpty() {
        Iterable<CurrencyRatesHandler> test = dbRepository.findAll();
        return !test.iterator().hasNext();
    }

    private void giveDataToDatabase(FxRatesHandling ans) {
        for (FxRateHandling fxRate : ans.getFxRate()) {
            dbRepository.save(
                    convertFxRateHandlingToCurrencyRatesHandler(fxRate)
            );
        }
    }

    //Converting values for db, BigDecimal remains as it is, others converted to string
    private CurrencyRatesHandler convertFxRateHandlingToCurrencyRatesHandler(FxRateHandling fxRate) {
        return new CurrencyRatesHandler(
                fxRate.getCcyAmt().get(1).getCcy().toString(),
                fxRate.getCcyAmt().get(1).getAmt(),
                LocalDate.of(fxRate.getDt().getYear(), fxRate.getDt().getMonth(), fxRate.getDt().getDay()),
                fxRate.getTp().toString()
        );
    }

    private FxRatesHandling loadFromDB() throws DatatypeConfigurationException {
        Iterable<CurrencyRatesHandler> iterableList = dbRepository.findAll();
        FxRatesHandling fxRates = new FxRatesHandling();
        for (CurrencyRatesHandler currencyRates : iterableList) {
            fxRates.getFxRate().add(convertCurrencyRatesHandlerToFxRateHandler(currencyRates));
        }
        return fxRates;
    }

    //converting values from db to main object handlers
    private FxRateHandling convertCurrencyRatesHandlerToFxRateHandler(CurrencyRatesHandler currencyRates) throws DatatypeConfigurationException {
        FxRateHandling fxRate = new FxRateHandling();
        //Because 1st CcyAmtHandling is always EUR with value of 1
        CcyAmtHandling temp1 = new CcyAmtHandling();
        temp1.setCcy(CcyISO4217.valueOf("EUR"));
        temp1.setAmt(new BigDecimal("1"));
        //Values from DB to 2nd CcyAmtHandling
        CcyAmtHandling temp2 = new CcyAmtHandling();
        temp2.setCcy(CcyISO4217.valueOf(currencyRates.getCcy()));
        temp2.setAmt(currencyRates.getAmt());
        //adding other values
        fxRate.getCcyAmt().add(temp1);
        fxRate.getCcyAmt().add(temp2);
        fxRate.setTp(FxRateTypeHandling.valueOf(currencyRates.getTp()));
        fxRate.setDt(DatatypeFactory.newInstance().newXMLGregorianCalendar(currencyRates.getDt().toString()));
        return fxRate;
    }

    public FxRatesHandling getCurrencyHistory(CcyISO4217 ccy, LocalDate startDate, LocalDate endDate) {
        LocalDate minimumDate = LocalDate.parse("2014-09-30");
        if(startDate.isBefore(minimumDate))
            startDate = minimumDate;
        if(endDate.isBefore(minimumDate))
            endDate = minimumDate;
        try {
            ResponseEntity<FxRatesHandling> returnEntity = restTemplate.getForEntity(URL_MAIN + URL_CURRENCY_HISTORY, FxRatesHandling.class, ccy, startDate, endDate);
            return returnEntity.getBody();
        } catch(HttpStatusCodeException e) {
            FxRatesHandling errorFxRates = new FxRatesHandling();
            OprlErrHandling error = new OprlErrHandling();
            error.setDesc("Http status code exception");
            ErrorCode errorCode = new ErrorCode();
            errorCode.setPrtry("500");
            error.setErr(errorCode);
            errorFxRates.setOprlErr(error);
            return errorFxRates;
        }





        //return restTemplate.getForObject(URL_MAIN + URL_CURRENCY_HISTORY, FxRatesHandling.class, ccy, startDate, endDate);
    }

    public CcyComparator getComparatorValuesForCcy(CcyComparator currencyComparator){
        if(currencyComparator.getCurrency1()==null || currencyComparator.getCurrency2() == null || currencyComparator.getAmount() == null)
            return new CcyComparator();

        setValueToCcyAmtHandling(currencyComparator.getCurrency1().getCcy(),
                currencyComparator.getCurrency1());
        setValueToCcyAmtHandling(currencyComparator.getCurrency2().getCcy(),
                currencyComparator.getCurrency2());
        return calculateRate(currencyComparator);
    }

    private void setValueToCcyAmtHandling(CcyISO4217 ccy, CcyAmtHandling ccyAmt) {
           if (ccy == CcyISO4217.EUR) {
            ccyAmt.setCcy(ccy);
            ccyAmt.setAmt(new BigDecimal("1"));
        } else {
            CurrencyRatesHandler currencyRatesHandler = dbRepository.findByCcy(ccy.toString());
               System.out.println("Currency rates handler - " + currencyRatesHandler);
            ccyAmt.setAmt(currencyRatesHandler.getAmt());
            ccyAmt.setCcy(ccy);
        }
    }

    private CcyComparator calculateRate(CcyComparator currencyComparator) { ;
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
