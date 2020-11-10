package testexample.currencyapiexample.quartz;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import lt.lb.webservices.fxrates.FxRatesHandling;
import testexample.currencyapiexample.repository.FxStorage;
import testexample.currencyapiexample.service.CcyComparatorService;
import testexample.currencyapiexample.service.FxClient;

@Component
public class DbUpdateTask extends QuartzJobBean {

    @Autowired
    private FxStorage fxStorage;
    
    @Autowired
    private FxClient fxClient;

    @Override
    protected void executeInternal(JobExecutionContext context) {
		FxRatesHandling fxRates = fxClient.requestRatesListFromAPI();
        fxStorage.updateDB(fxRates);
    }
}
