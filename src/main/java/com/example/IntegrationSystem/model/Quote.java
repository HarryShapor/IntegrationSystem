package com.example.IntegrationSystem.model;


public class Quote {

    public String marketName;
    public double value;
    public long volume;
    public String tradedate;

    public Quote(String marketName, double value, long volume, String tradedate) {
        this.marketName = marketName;
        this.value = value;
        this.volume = volume;
        this.tradedate = tradedate;
    }

    @Override
    public String toString() {
        return this.marketName + " " + this.value + " " + this.volume + " " + this.tradedate;
    }
}
