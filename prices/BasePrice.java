package com.iohk.prices;

import com.iohk.Bid;
import com.iohk.coin.CoinType;

import java.util.List;

public class BasePrice {

    public final CoinType coinType;
    public final double value;

    private final double thresholdRatio = 0.01;
    private final double reductionRatio = 0.95;
    private final double maxDiffRatio = 0.25;
    private final double adjustmentRatio = 0.9;

    public BasePrice(CoinType coinType, double value) throws Exception {
        if(value < 0) {
            throw new Exception("Value is negative");
        }
        this.coinType = coinType;
        this.value = value;
    }

    public BasePrice updated(List<Bid> winningBids, double coinMedianPrice) throws Exception {
        if(coinMedianPrice < 0) {
            throw new Exception("Median price is negative");
        }
        double soldTotal = 0, soldPerCoin = 0;
        for(Bid b : winningBids){
            if(b.coinsBurnt.coinType == coinType){
                soldPerCoin += b.dustsToGet;
            }
            soldTotal += b.dustsToGet;
        }

        double newValue = coinMedianPrice;
        if(soldPerCoin < soldTotal * thresholdRatio){
            newValue *= reductionRatio;
        }
        if((soldPerCoin == 0) || ((value - newValue) > value * maxDiffRatio)){
            newValue = value * adjustmentRatio;
        }
        return new BasePrice(coinType, newValue);
    }

    public String toString(){ return (coinType.toString() + " : " + value); }
}