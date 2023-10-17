package com.iohk.utils;

import com.iohk.Bid;
import com.iohk.coin.CoinPoB;
import com.iohk.coin.CoinType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import static java.lang.System.out;

import static java.lang.Math.*;
import static java.lang.Math.log;
import static java.lang.Math.pow;

public class Utils {

    public static List<CoinType> set_coin_types(List<String> coinsList){
        return new ArrayList<>(coinsList).stream().map(CoinType::new).collect(Collectors.toList());
    }

    public static List<Bid> generate_bids(CoinType coinType, int participantsNum) throws Exception {
        String idPrefix = "participant";
        Random rand = new Random();
        List<Bid> bids = new ArrayList<>();
        for(int i = 0; i < participantsNum; i++){
            bids.add(new Bid(idPrefix + i, new CoinPoB(coinType, abs(rand.nextDouble())), abs(rand.nextDouble())));
        }
        return bids;
    }

    public static List<Bid> generate_bids(List<CoinPoB> coins) throws Exception {
        String idPrefix = "participant";
        Random rand = new Random();
        List<Bid> bids = new ArrayList<>();
        int prefix_counter = 0;
        for(CoinPoB coin : coins){
            bids.add(new Bid(idPrefix + prefix_counter++, coin, abs(rand.nextDouble())));
        }
        return bids;
    }

    public static List<Bid> generate_normal_bids(CoinType coinType, int participantsNum, double price, double r) throws Exception {
        String idPrefix = "honest participant";
        String idPrefix2 = "strategic participant";
        //double btcExchangePrice = 0.000125;
        Random rand = new Random();
        List<Bid> bids = new ArrayList<>();
        for(int i = 0; i < participantsNum*0.5; i++){
            //out.println(participantsNumh);
            double myPrice = rand.nextGaussian() * (price * 0.1) + (price * 0.9);
            //double myPrice = exp(myPricee);
            //double burnNum = abs(rand.nextGaussian() * (r*2) + r);
            double burnNum = 0.0001 + (r - 0.0001) * rand.nextDouble();
            double dustsGet = burnNum / myPrice;
            bids.add(new Bid(idPrefix, new CoinPoB(coinType, burnNum), dustsGet));
        }
        for(int i = (int)(participantsNum*0.5); i < participantsNum; i++){
            //out.println(participantsNums);
            //double myPrice = rand.nextGaussian() * (price * 0.04) + (price);
            double myPrice = rand.nextGaussian() * (price * 0.1) + (price * 0.5);
            //double myPrice = exp(myPricee);
            //double burnNum = abs(rand.nextGaussian() * (r*2) + r);
            //double burnNum = 0.0001 + (r - 0.0001) * rand.nextDouble();
            double burnNum = 0.0001 + (r - 0.0001) * rand.nextDouble();
            double dustsGet = burnNum / myPrice;
            bids.add(new Bid(idPrefix2, new CoinPoB(coinType, burnNum), dustsGet));
        }
        return bids;
    }

    public static List<Bid> generate_log_normal_bids(CoinType coinType, int participantsNum, double price, double r) throws Exception {
        String idPrefix = "honest participant";
        String idPrefix2 = "strategic participant";
        //double btcExchangePrice = 0.000125;
        double d_log = price * 0.04, mu_log = price * 0.9;
        double d = sqrt(log(pow(d_log/mu_log, 2.0)+1));
        double mu = log(mu_log) - (pow(d,2.0)/2);
        Random rand = new Random();
        List<Bid> bids = new ArrayList<>();
        for(int i = 0; i < (int)(participantsNum*0.9); i++){
            double myPricee = rand.nextGaussian() * (d) + (mu);
            double myPrice = exp(myPricee);
            //double burnNum = abs(rand.nextGaussian() * (r*2) + r);
            double burnNum = 0.0001 + (r - 0.0001) * rand.nextDouble();
            double dustsGet = burnNum / myPrice;
            bids.add(new Bid(idPrefix + i, new CoinPoB(coinType, burnNum), dustsGet));
        }
        double d_log2 = price * 0.04, mu_log2 = price * 0.5;
        double d2 = sqrt(log(pow(d_log2/mu_log2, 2.0)+1));
        double mu2 = log(mu_log2) - (pow(d2,2.0)/2);
        for(int i = (int)(participantsNum*0.9); i < participantsNum; i++){
            double myPricee = rand.nextGaussian() * (d2) + (mu2);
            double myPrice = exp(myPricee);
            //double burnNum = abs(rand.nextGaussian() * (r*2) + r);
            double burnNum = 0.0001 + (r - 0.0001) * rand.nextDouble();
            double dustsGet = burnNum / myPrice;
            bids.add(new Bid(idPrefix2, new CoinPoB(coinType, burnNum), dustsGet));
        }
        return bids;
    }

    public static List<Bid> generate_low_bids(CoinType coinType, int participantsNum, double price, double r) throws Exception {
        String idPrefix = "participant";
        //double btcExchangePrice = 0.000125;
        Random rand = new Random();
        List<Bid> bids = new ArrayList<>();
        for(int i = 0; i < participantsNum; i++){
            double myPrice = rand.nextGaussian() * (price * 0.04) + (price * 0.7);
            //double burnNum = abs(rand.nextGaussian() * (r*2) + r);
            double burnNum = 0.0001 + (r - 0.0001) * rand.nextDouble();
            double dustsGet = burnNum / myPrice;
            bids.add(new Bid(idPrefix + i, new CoinPoB(coinType, burnNum), dustsGet));
        }
        return bids;
    }

    public static List<Bid> update_bids_price(List<Bid> bids, double percent) throws Exception {
        if(percent < -1 || percent > 1){
            throw new Exception("Incorrect value of percent");
        }
        List<Bid> updated_bids = new ArrayList<>();
        for(Bid bid : bids){
            updated_bids.add(bid.updated_by_price(percent));
        }
        return updated_bids;
    }
}