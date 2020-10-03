package testexample.currencyapiexample.model;

import models.CcyAmtHandling;
import org.springframework.format.annotation.NumberFormat;

import javax.validation.constraints.*;
import java.math.BigDecimal;

public class CcyComparator {

    private CcyAmtHandling comparable1;
    private CcyAmtHandling comparable2;
    private boolean isActive;

    @NotNull
    @Min(0)
    private BigDecimal conversionAmount;

    public CcyComparator(CcyAmtHandling comparable1, CcyAmtHandling comparable2) {
        this.comparable1 = comparable1;
        this.comparable2 = comparable2;
        this.isActive=false;
    }

    public CcyComparator() {
        this.isActive=false;
    }

    public CcyAmtHandling getComparable1() {
        return comparable1;
    }

    public void setComparable1(CcyAmtHandling comparable1) {
        this.comparable1 = comparable1;
    }

    public CcyAmtHandling getComparable2() {
        return comparable2;
    }

    public void setComparable2(CcyAmtHandling comparable2) {
        this.comparable2 = comparable2;
    }

    @Override
    public String toString() {
        return "CompareCurrency{" +
                "comparable1=" + comparable1 +
                ", comparable2=" + comparable2 +
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
}
