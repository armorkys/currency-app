package testexample.currencyapiexample.quartz;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import testexample.currencyapiexample.service.MainService;

@Component
public class DbUpdateTask extends QuartzJobBean {

    @Autowired
    private MainService mainService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        mainService.updateDB();
    }
}
