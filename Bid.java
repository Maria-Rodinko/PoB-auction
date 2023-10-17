package com.iohk;

import com.iohk.coin.CoinPoB;
import com.iohk.coin.CoinType;
import com.iohk.prices.BasePrice;

import java.util.Map;

public class Bid {

    public final String  id;
    public final CoinPoB coinsBurnt;
    public final double  dustsToGet;
    private double relativePrice = 0;

    public Bid(String id, CoinPoB coinsBurnt, double dustsToGet) throws Exception {
        if(dustsToGet < 0) {
            throw new Exception("dustsToGet is negative");
        }
        this.id = id;
        this.coinsBurnt = coinsBurnt;
        this.dustsToGet = dustsToGet;
    }

    public double price(){ return (coinsBurnt.value / dustsToGet); }

    public void set_relative_price(Map<CoinType, BasePrice> basePrices){
        relativePrice = (price() / basePrices.get(coinsBurnt.coinType).value);
    }

    public double relative_price(){ return relativePrice; }

    // Recalculates bid according to a new price that is bigger or lesser by specified `percent` value
    public Bid updated_by_price(double percent) throws Exception {
        if(percent < -1 || percent > 1){
            throw new Exception("Incorrect value of percent");
        }
        if(this.id.equals("strategic participant"))
            return (new Bid(this.id, this.coinsBurnt, this.dustsToGet));
        return (new Bid(this.id, this.coinsBurnt, coinsBurnt.value / (price() * (1 + percent))));
    }

    public String toString(){
        return ("id: "            + id                     + "\n" +
                "coinsBurnt:    " + coinsBurnt.toString()  + "\n" +
                "dustsToGet:    " + dustsToGet             + "\n" +
                "price:         " + price()                + "\n" +
                "relativePrice: " + relativePrice          + "\n");
    }
}