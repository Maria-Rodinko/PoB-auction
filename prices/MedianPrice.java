package com.iohk.prices;

import com.iohk.Bid;
import com.iohk.coin.CoinType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MedianPrice {

    public final CoinType coinType;
    public final double value;
    public final double relativeValue;

    public MedianPrice(CoinType coinType, List<Bid> bids) {

        this.coinType = coinType;

        List<Bid> coinBids = bids.stream()
                .filter(b -> b.coinsBurnt.coinType == coinType)
                .sorted((a, b) -> Double.compare(b.price(), a.price()))
                .collect(Collectors.toList());

        if(coinBids.isEmpty()){
            value = 0;
            relativeValue = 0;
            return;
        }
        if(coinBids.size() <= 2){
            Bid medianBid = coinBids.get(coinBids.size() - 1);
            value = medianBid.price();
            relativeValue = medianBid.relative_price();
            return;
        }

        List<Double> t = new ArrayList<>();
        double t_k = 0;
        for (Bid b : coinBids){
            t_k += b.dustsToGet;
            t.add(t_k);
        }
        double t_av = t_k / 2;

        for(int i = 0; i < t.size() - 1; i++){
            if(t.get(i) < t_av && t_av <= t.get(i + 1)){
                Bid medianBid = coinBids.get(i + 1);
                value = medianBid.price();
                relativeValue = medianBid.relative_price();
                return;
            }
        }
        value = 0;
        relativeValue = 0;
    }

    public String toString(){ return (coinType.toString() + " : " + value); }
}