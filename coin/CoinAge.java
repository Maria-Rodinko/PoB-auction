package com.iohk.coin;

public class CoinAge {

    public final CoinPoB coin;
    public final int age;

    public CoinAge(CoinPoB coin){
        this.coin = coin;
        this.age = 1;
    }

    private CoinAge(CoinPoB coin, int age){
        this.coin = coin;
        this.age = age;
    }

    public CoinAge incremented(){ return new CoinAge(coin, age + 1); }
    public String toString(){ return (coin + " : " + age); }
}