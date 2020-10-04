package testexample.currencyapiexample.model;

import lt.lb.webservices.fxrates.CcyAmtHandling;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Objects;

public class CcyComparator {

    @NotNull(message = "Must not be null")
    private CcyAmtHandling currency1;
    @NotNull(message = "Must not be null")
    private CcyAmtHandling currency2;

    private boolean isActive;

    @Digits(integer = 20, fraction = 2)
    @DecimalMin(value = "0", inclusive = false, message = "Must be more than 0")
    @NotNull(message = "Must not be null")
    private BigDecimal conversionAmount;




    public CcyComparator(CcyAmtHandling currency1, CcyAmtHandling currency2) {
        this.currency1 = currency1;
        this.currency2 = currency2;
        this.isActive=false;
    }

    public CcyComparator() {
        this.isActive=false;
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

    @Override
    public String toString() {
        return "CompareCurrency{" +
                "currency 1=" + currency1 +
                ", currency 2=" + currency2 +
                '}';
    }

    public boolean getActive() {
        return this.isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public BigDecimal getConversionAmount() {
        return conversionAmount;
    }

    public void setConversionAmount(BigDecimal conversionAmount) {
        this.conversionAmount = conversionAmount;
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CcyComparator that = (CcyComparator) o;
        return isActive == that.isActive &&
                Objects.equals(currency1, that.currency1) &&
                Objects.equals(currency2, that.currency2) &&
                Objects.equals(conversionAmount, that.conversionAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency1, currency2, isActive, conversionAmount);
    }
}
