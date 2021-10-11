package model;

import java.util.Objects;

public class CurrencyPairRate {
    private String ccyPair;
    private double rate;

    public CurrencyPairRate(String ccyPair, double rate) {
        this.ccyPair = ccyPair;
        this.rate = rate;
    }

    public String getCcyPair() {
        return ccyPair;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CurrencyPairRate pairRate = (CurrencyPairRate) o;

        if (Double.compare(pairRate.rate, rate) != 0) return false;
        return Objects.equals(ccyPair, pairRate.ccyPair);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = ccyPair != null ? ccyPair.hashCode() : 0;
        temp = Double.doubleToLongBits(rate);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
