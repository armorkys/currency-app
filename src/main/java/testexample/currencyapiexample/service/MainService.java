package testexample.currencyapiexample.service;

import lt.lb.webservices.fxrates.*;
import org.assertj.core.util.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import testexample.currencyapiexample.model.CcyComparator;
import testexample.currencyapiexample.model.CurrencyRatesHandler;
import testexample.currencyapiexample.repository.CurrencyRatesHandlerRepository;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class MainService {

    @VisibleForTesting
    static final String URLMAIN = "http://www.lb.lt/webservices/FxRates/FxRates.asmx/";

    @Autowired
    CurrencyRatesHandlerRepository dbRepository;

    @Autowired
    @VisibleForTesting
    RestTemplate restTemplate;

    public FxRatesHandling requestRatesListFromAPI() {
        return restTemplate.getForObject(URLMAIN + "getCurrentFxRates?tp=EU", FxRatesHandling.class);
    }

    public void updateDB() throws DatatypeConfigurationException {
        FxRatesHandling fxRates = requestRatesListFromAPI();
        if(fxRates.getFxRate().iterator().hasNext()) {
            dbRepository.deleteAll();
            giveDataToDatabase(fxRates);
         } else if(!fxRates.getFxRate().iterator().hasNext()){
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

    private void giveDataToDatabase(FxRatesHandling ans){
        CurrencyRatesHandler template = new CurrencyRatesHandler();
        //Converting values for db, BigDecimal remains as it is, others converted to string
        for (FxRateHandling handle1 : ans.getFxRate()) {
            dbRepository.save(new CurrencyRatesHandler(
                    handle1.getCcyAmt().get(1).getCcy().toString(),
                    handle1.getCcyAmt().get(1).getAmt(),
                    LocalDate.of(handle1.getDt().getYear(), handle1.getDt().getMonth(), handle1.getDt().getDay()),
                    handle1.getTp().toString()
            ));
        }
    }

    private FxRatesHandling loadFromDB() throws DatatypeConfigurationException {
        Iterable<CurrencyRatesHandler> iterableList = dbRepository.findAll();
        FxRatesHandling mainHandle = new FxRatesHandling();
        //converting values from db to main object handlers
        for (CurrencyRatesHandler handle : iterableList) {
            FxRateHandling tempHandle = new FxRateHandling();
            CcyAmtHandling temp1 = new CcyAmtHandling();
            temp1.setCcy(CcyISO4217.valueOf("EUR"));
            temp1.setAmt(new BigDecimal("1"));
            CcyAmtHandling temp2 = new CcyAmtHandling();
            temp2.setCcy(CcyISO4217.valueOf(handle.getCcy()));
            temp2.setAmt(handle.getAmt());
            tempHandle.getCcyAmt().add(temp1);
            tempHandle.getCcyAmt().add(temp2);
            tempHandle.setTp(FxRateTypeHandling.valueOf(handle.getTp()));
            tempHandle.setDt(DatatypeFactory.newInstance().newXMLGregorianCalendar(handle.getDt().toString()));
            mainHandle.getFxRate().add(tempHandle);
        }
        return mainHandle;
    }

    public FxRatesHandling getCurrencyHistoryBase(String ccy, String startDate, String endDate) {
        return restTemplate.getForObject(URLMAIN + "getFxRatesForCurrency?tp=EU&ccy=" + ccy + "&dtFrom=" + startDate + "&dtTo=" + endDate, FxRatesHandling.class);
    }

    public CcyComparator compareCcyRate(CcyComparator comparatorRes) throws DatatypeConfigurationException {
        CcyISO4217 cc3;
        cc3 = comparatorRes.getCurrency1().getCcy();
        CcyISO4217 ccy1 = comparatorRes.getCurrency1().getCcy();
        CcyISO4217 ccy2 = comparatorRes.getCurrency2().getCcy();
        CcyComparator comparator = new CcyComparator(new CcyAmtHandling(), new CcyAmtHandling());
        List<FxRateHandling> currencyList = getCurrentCurrencyRates().getFxRate();
        //checking for equal ccy enums to the ones loaded from DB and assigning amt value
        for (FxRateHandling handle : currencyList) {
            checkHandleToCcyISO(handle, ccy1, comparator.getCurrency1(), 0);
            checkHandleToCcyISO(handle, ccy2, comparator.getCurrency2(), 0);
            checkHandleToCcyISO(handle, ccy1, comparator.getCurrency1(), 1);
            checkHandleToCcyISO(handle, ccy2, comparator.getCurrency2(), 1);
        }
        comparator.setConversionAmount(comparatorRes.getConversionAmount());
        return calculateRate(comparator);
    }

    private void checkHandleToCcyISO(FxRateHandling handle, CcyISO4217 ccy, CcyAmtHandling comparable, int step) {
        if (handle.getCcyAmt().get(step).getCcy() == ccy) {
            comparable.setCcy(handle.getCcyAmt().get(step).getCcy());
            comparable.setAmt(handle.getCcyAmt().get(step).getAmt());
        }

    }

    private CcyComparator calculateRate(CcyComparator comparator) {
        BigDecimal multiplier = comparator.getConversionAmount();
        BigDecimal val1 = comparator.getCurrency1().getAmt();
        BigDecimal val2 = comparator.getCurrency2().getAmt();
        val2 = val2.divide(val1, 6, RoundingMode.CEILING);
        val1 = val1.divide(val1, 6, RoundingMode.CEILING);
        val1 = val1.multiply(multiplier);
        val2 = val2.multiply(multiplier);
        comparator.getCurrency1().setAmt(val1);
        comparator.getCurrency2().setAmt(val2);
        return comparator;
    }

}
