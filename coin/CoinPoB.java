package com.iohk.coin;

import java.util.Random;

public class CoinPoB {

    public final CoinType coinType;
    public final double value;
    public final long pobIdentifier; // simulates unique PoB proof

    public CoinPoB(CoinType coinType, double value) throws Exception {
        if(value < 0) {
            throw new Exception("Value is negative");
        }
        this.coinType = coinType;
        this.value = value;
        this.pobIdentifier = new Random().nextLong();
    }

    public String toString(){ return (value + " " + coinType.toString() + "; PoB: " + Long.toHexString(pobIdentifier)); }
}