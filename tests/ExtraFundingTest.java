package com.iohk.tests;

import com.iohk.Auction;
import com.iohk.Bid;
import com.iohk.coin.CoinPoB;
import com.iohk.coin.CoinType;
import com.iohk.prices.BasePrice;

import java.util.*;

import static java.lang.System.out;

public class ExtraFundingTest {

    static private CoinType btc = new CoinType("btc");
    static private CoinType etc = new CoinType("etc");

    private static List<Bid> set_bids() throws Exception {
        String idPrefix = "participant";
        List<Bid> bids = new ArrayList<>();

        bids.add(new Bid(idPrefix + 1,  new CoinPoB(btc, 0.1), 750));
        bids.add(new Bid(idPrefix + 2,  new CoinPoB(btc, 0.15), 1140));
        bids.add(new Bid(idPrefix + 3,  new CoinPoB(etc, 10), 2080));
        bids.add(new Bid(idPrefix + 4,  new CoinPoB(etc, 15), 3150));
        bids.add(new Bid(idPrefix + 5,  new CoinPoB(btc, 0.05), 420));
        bids.add(new Bid(idPrefix + 6,  new CoinPoB(etc, 5), 1100));
        bids.add(new Bid(idPrefix + 7,  new CoinPoB(btc, 0.03), 265));
        bids.add(new Bid(idPrefix + 8,  new CoinPoB(btc, 0.2), 1790));
        bids.add(new Bid(idPrefix + 9,  new CoinPoB(btc, 1), 9100));
        bids.add(new Bid(idPrefix + 10, new CoinPoB(btc, 0.12), 1100));
        bids.add(new Bid(idPrefix + 11, new CoinPoB(etc, 2), 470));

        return bids;
    }

    private static boolean test(boolean verbose) throws Exception {

        double fundRegularVolume = 10000, fundExtraVolume = 5000;
        int maxCoinAge = 2;

        Map<CoinType, BasePrice> basePrices = new TreeMap<>();
        basePrices.put(btc, new BasePrice(btc, 0.000125));
        basePrices.put(etc, new BasePrice(etc, 0.005));

        Auction auction = new Auction(fundRegularVolume, fundExtraVolume, maxCoinAge, basePrices);
        auction.add_bids(set_bids());
        auction.select_winners();

        if(verbose){
            out.print(auction.toString());
        }
        return (auction.is_finished() && auction.reserve_is_needed());
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