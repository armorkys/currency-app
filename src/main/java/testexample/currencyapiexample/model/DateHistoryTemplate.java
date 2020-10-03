package testexample.currencyapiexample.model;

import models.CcyISO4217;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Date;

public class DateHistoryTemplate {

    private CcyISO4217 ccy;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotEmpty
    @PastOrPresent
    private Date startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotEmpty
    @PastOrPresent
    private Date endDate;

    public DateHistoryTemplate() {
    }

    public DateHistoryTemplate(CcyISO4217 ccy, Date startDate, Date endDate) {
        this.ccy = ccy;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public CcyISO4217 getCcy() {
        return ccy;
    }

    public void setCcy(CcyISO4217 ccy) {
        this.ccy = ccy;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
