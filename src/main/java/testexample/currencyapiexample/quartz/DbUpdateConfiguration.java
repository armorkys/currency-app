package testexample.currencyapiexample.quartz;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.quartz.TriggerBuilder.newTrigger;

@Configuration
public class DbUpdateConfiguration {

    @Bean
    public JobDetail scheduleJob() {
        return JobBuilder.newJob(DbUpdateTask.class).storeDurably().withIdentity("db_update")
                .withDescription("Database update task").build();
    }

    @Bean
    public Trigger scheduleTrigger() {
        return newTrigger().withIdentity("db_update_trigger").forJob(scheduleJob())
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 ? * * *"))
                .build();
    }
}
