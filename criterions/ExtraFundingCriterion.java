package com.iohk.criterions;

import com.iohk.Bid;
import com.iohk.coin.CoinType;
import com.iohk.prices.MedianPrice;

import java.util.*;
import java.util.stream.Collectors;

public class ExtraFundingCriterion {

    private final Set<CoinType> coinsTypes;
    private final List<Bid> winningBids;
    private final List<Bid> lostBids;
    private final Map<CoinType, MedianPrice> medianPricesWinning;
    private final Map<CoinType, MedianPrice> medianPricesLost;

    public ExtraFundingCriterion(){
        coinsTypes = new HashSet<>();
        winningBids = new ArrayList<>();
        lostBids = new ArrayList<>();
        medianPricesWinning = new HashMap<>();
        medianPricesLost = new HashMap<>();
    }

    public ExtraFundingCriterion(Set<CoinType> coinsTypes,
                                 List<Bid> winningBids,
                                 List<Bid> lostBids,
                                 Map<CoinType, MedianPrice> medianPricesWinning,
                                 Map<CoinType, MedianPrice> medianPricesLost){
        this.coinsTypes = coinsTypes;
        this.winningBids = winningBids;
        this.lostBids = lostBids;
        this.medianPricesWinning = medianPricesWinning;
        this.medianPricesLost = medianPricesLost;
    }

    private boolean criterion1(){
        double volumeWinning = 0, volumeLost = 0;
        for(CoinType coinType : coinsTypes){
            List<Bid> bidsWinning = winningBids.stream().filter(b -> b.coinsBurnt.coinType == coinType).collect(Collectors.toList());
            List<Bid> bidsLost = lostBids.stream().filter(b -> b.coinsBurnt.coinType == coinType).collect(Collectors.toList());

            double median_price_w = medianPricesWinning.get(coinType).value;
            for(Bid b : bidsWinning){
                volumeWinning += b.coinsBurnt.value / median_price_w;
            }
            for(Bid b : bidsLost){
                volumeLost += b.coinsBurnt.value / median_price_w;
            }
        }
        return (volumeLost >= volumeWinning);
    }

    public boolean reduce_criterion(){
        double volumeWinning = 0, volumeLost = 0;
        for(CoinType coinType : coinsTypes){
            List<Bid> bidsWinning = winningBids.stream().filter(b -> b.coinsBurnt.coinType == coinType).collect(Collectors.toList());
            List<Bid> bidsLost = lostBids.stream().filter(b -> b.coinsBurnt.coinType == coinType).collect(Collectors.toList());

            double median_price_w = medianPricesWinning.get(coinType).value;
            for(Bid b : bidsWinning){
                volumeWinning += b.coinsBurnt.value / median_price_w;
            }
            for(Bid b : bidsLost){
                volumeLost += b.coinsBurnt.value / median_price_w;
            }
        }
        return (volumeLost < 0.2*volumeWinning);
    }

    private boolean criterion2(){
        double soldTotalWinning = 0, soldTotalLost = 0;
        for(Bid b : winningBids){
            soldTotalWinning += b.dustsToGet;
        }
        for(Bid b : lostBids){
            soldTotalLost += b.dustsToGet;
        }

        double winningIndicator = 0, lostIndicator = 0;

        for(CoinType coinType : coinsTypes){
            List<Bid> bidsWinning = winningBids.stream().filter(b -> b.coinsBurnt.coinType == coinType).collect(Collectors.toList());
            List<Bid> bidsLost = lostBids.stream().filter(b -> b.coinsBurnt.coinType == coinType).collect(Collectors.toList());

            double soldPerCoinWinning = 0, soldPerCoinLost = 0;
            for(Bid b : bidsWinning){
                soldPerCoinWinning += b.dustsToGet;
            }
            for(Bid b : bidsLost){
                soldPerCoinLost += b.dustsToGet;
            }

            winningIndicator += soldPerCoinWinning / soldTotalWinning * medianPricesWinning.get(coinType).relativeValue;
            lostIndicator += soldPerCoinLost / soldTotalLost * medianPricesLost.get(coinType).relativeValue;
        }
        return (lostIndicator >= 0.85 * winningIndicator);
    }

    public boolean is_true(){ return (criterion1() && criterion2()); }
}