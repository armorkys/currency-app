package testexample.currencyapiexample.model;

import lt.lb.webservices.fxrates.CcyAmtHandling;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

public class CcyComparator {

    @NotNull(message = "Must not be null")
    private CcyAmtHandling currency1;
    @NotNull(message = "Must not be null")
    private CcyAmtHandling currency2;

    @Digits(integer = 20, fraction = 2)
    @DecimalMin(value = "0", inclusive = false, message = "Must be more than 0")
    @NotNull(message = "Must not be null")
    private BigDecimal amount;

    public CcyComparator(CcyAmtHandling currency1, CcyAmtHandling currency2) {
        this.currency1 = currency1;
        this.currency2 = currency2;
    }

    public CcyComparator() {
    }

    public CcyAmtHandling getCurrency1() {
        return currency1;
    }

    public void setCurrency1(CcyAmtHandling currency1) {
        this.currency1 = currency1;
    }

    public CcyAmtHandling getCurrency2() {
        return currency2;
    }

    public void setCurrency2(CcyAmtHandling currency2) {
        this.currency2 = currency2;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "CcyComparator{" +
                "currency1=" + currency1 +
                ", currency2=" + currency2 +
                ", amount=" + amount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CcyComparator that = (CcyComparator) o;
        return Objects.equals(currency1, that.currency1) &&
                Objects.equals(currency2, that.currency2) &&
                Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency1, currency2, amount);
    }
}
