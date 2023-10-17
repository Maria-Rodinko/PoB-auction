package com.iohk.tests;

import com.iohk.Auction;
import com.iohk.Bid;
import com.iohk.coin.CoinPoB;
import com.iohk.coin.CoinType;
import com.iohk.prices.BasePrice;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static java.lang.System.out;

public class NormalDistrTest {
    static private CoinType btc = new CoinType("btc");
    static private double btcBasePrice = 0.000125;
    static private double btcExchangePrice = btcBasePrice;

    private static List<CoinType> set_coin_types(List<String> coinsList){
        return new ArrayList<>(coinsList).stream().map(CoinType::new).collect(Collectors.toList());
    }

    private static List<Bid> generate_bids(CoinType coinType, int participantsNum) throws Exception {
        String idPrefix = "participant";
        Random rand = new Random();
        List<Bid> bids = new ArrayList<>();
        for(int i = 0; i < participantsNum; i++){
            double myPrice = rand.nextGaussian() * (btcExchangePrice * 0.04) + (btcExchangePrice * 0.9);
            double burnNum = abs(rand.nextDouble());
            double dustsGet = burnNum / myPrice;
            bids.add(new Bid(idPrefix + i, new CoinPoB(coinType, burnNum), dustsGet));
            //bids.add(new Bid(idPrefix + i, new CoinPoB(coinType, abs(rand.nextDouble())), abs(rand.nextDouble())));
        }
        return bids;
    }

    private static boolean test(boolean verbose) throws Exception {
        int epochsNumber = 20, maxParticipants = 100, maxCoinAge = 10;
        double fundRegularVolume = 10000, fundExtraVolume = 5000;

        List<CoinType> coinTypes = set_coin_types(Arrays.asList("btc"));

        Random rand = new Random();
        Map<CoinType, BasePrice> basePrices = new TreeMap<>();
        /*for(CoinType coinType : coinTypes){
            basePrices.put(coinType, new BasePrice(coinType, abs(rand.nextDouble())));
        }*/
        basePrices.put(btc, new BasePrice(btc, btcBasePrice));

        Optional<Auction> prevAuction = Optional.empty();

        for(int epoch = 0; epoch < epochsNumber; epoch++){
            Auction auction;
            if(!prevAuction.isPresent()){
                auction = new Auction(fundRegularVolume, fundExtraVolume, maxCoinAge, basePrices);
            } else {
                auction = new Auction(prevAuction.get());
            }

            int participantsNumber = new Random().nextInt(maxParticipants) + 1;
            for(CoinType coinType : coinTypes){
                auction.add_bids(generate_bids(coinType, participantsNumber));
            }
            auction.select_winners();
            if(verbose){
                out.print(auction.toString());
            }
            prevAuction = Optional.of(auction);
        }
        return true;
    }

    public static boolean run(boolean verbose){
        try {
            return test(verbose);
        } catch (Exception e){
            if(verbose){
                out.println(e.getMessage());
            }
            return false;
        }
    }
}
