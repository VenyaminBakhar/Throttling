package model;

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
}
