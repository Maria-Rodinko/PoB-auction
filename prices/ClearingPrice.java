package com.iohk.prices;

import com.iohk.coin.CoinType;

public class ClearingPrice {
    public final CoinType coinType;
    public final double value;

    public ClearingPrice(){ coinType = new CoinType(""); value = 0; }

    public ClearingPrice(CoinType coinType, double value) {
        this.coinType = coinType;
        this.value = Math.abs(value);
    }

    public String toString(){ return (coinType.toString() + " : " + value); }
}