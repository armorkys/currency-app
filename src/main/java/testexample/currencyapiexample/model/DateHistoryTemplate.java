package testexample.currencyapiexample.model;

import lt.lb.webservices.fxrates.CcyISO4217;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.Objects;

public class DateHistoryTemplate {

    private CcyISO4217 ccy;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @PastOrPresent
    @NotNull
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @PastOrPresent
    @NotNull
    private LocalDate endDate;

    public DateHistoryTemplate() {
    }

    public DateHistoryTemplate(CcyISO4217 ccy, LocalDate startDate, LocalDate endDate) {
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateHistoryTemplate that = (DateHistoryTemplate) o;
        return ccy == that.ccy &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ccy, startDate, endDate);
    }
}
