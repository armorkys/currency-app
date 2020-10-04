package testexample.currencyapiexample.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table
public class CurrencyRatesHandler {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column
    private String ccy;

    @Column
    private BigDecimal amt;

    @Column
    private LocalDate dt;

    @Column
    private String tp;


    public CurrencyRatesHandler(String ccy, BigDecimal amt, LocalDate dt, String tp) {
        this.ccy = ccy;
        this.amt = amt;
        this.dt = dt;
        this.tp = tp;
    }

    public CurrencyRatesHandler() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCcy() {
        return ccy;
    }

    public void setCcy(String ccy) {
        this.ccy = ccy;
    }

    public BigDecimal getAmt() {
        return amt;
    }

    public void setAmt(BigDecimal amt) {
        this.amt = amt;
    }

    public LocalDate getDt() {
        return dt;
    }

    public void setDt(LocalDate dt) {
        this.dt = dt;
    }

    public String getTp() {
        return tp;
    }

    public void setTp(String tp) {
        this.tp = tp;
    }

    @Override
    public String toString() {
        return "CurrencyRatesHistory{" +
                "id=" + id +
                ", ccy=" + ccy +
                ", ant=" + amt +
                ", dt=" + dt +
                ", tp=" + tp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CurrencyRatesHandler that = (CurrencyRatesHandler) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(ccy, that.ccy) &&
                Objects.equals(amt, that.amt) &&
                Objects.equals(dt, that.dt) &&
                Objects.equals(tp, that.tp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ccy, amt, dt, tp);
    }
}
