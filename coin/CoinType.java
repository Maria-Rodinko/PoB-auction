package com.iohk.coin;

public class CoinType implements Comparable<CoinType> {

    private final String coinType;

    public CoinType(String coinType){ this.coinType = coinType; }
    public String toString(){ return coinType; }
    public int compareTo(CoinType other){
        return this.coinType.compareTo(other.coinType);
    }
}