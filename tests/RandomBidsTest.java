package com.iohk.tests;

import com.iohk.Auction;
import com.iohk.Bid;
import com.iohk.coin.CoinAge;
import com.iohk.coin.CoinPoB;
import com.iohk.coin.CoinType;
import com.iohk.prices.BasePrice;
import com.iohk.utils.Utils;

import java.util.*;

import static java.lang.Math.abs;
import static java.lang.System.out;

public class RandomBidsTest {

    private int age;

    private static boolean test(boolean verbose) throws Exception {

        int epochsNumber = 10, maxParticipants = 100, maxCoinAge = 100;
        double fundRegularVolume = 100000, fundExtraVolume = 50000 ;
        double [] pr = new double[]{0.000125, 0.005};
        double [] r = new double[]{0.5, 10};

        List<CoinType> coinTypes = Utils.set_coin_types(Arrays.asList("btc", "etc"));

        Random rand = new Random();
        Map<CoinType, BasePrice> basePrices = new TreeMap<>();
        /*for(CoinType coinType : coinTypes){
            basePrices.put(coinType, new BasePrice(coinType, abs(rand.nextDouble())));
        }*/
        basePrices.put(coinTypes.get(0), new BasePrice(coinTypes.get(0), 0.000125));
        basePrices.put(coinTypes.get(1), new BasePrice(coinTypes.get(1), 0.005));

        Optional<Auction> prevAuction = Optional.empty();
        List<CoinPoB> unspent_coins = new ArrayList<>();
        List<Bid> lost_bids = new ArrayList<>();

        for(int epoch = 0; epoch < epochsNumber; epoch++){

            //out.println("------------------------------------------------------------------------------------------------------------------------");
            //out.println("Simulating Epoch " + epoch);
            //out.println("------------------------------------------------------------------------------------------------------------------------");

            Auction auction;
            if(!prevAuction.isPresent()){
                auction = new Auction(fundRegularVolume, fundExtraVolume, maxCoinAge, basePrices);
            } else {
                auction = new Auction(prevAuction.get());
            }

            // Generating new random bids
            int i = 0;
            //int participantsNumber = new Random().nextInt(maxParticipants) + 1;
            for(CoinType coinType : coinTypes){
                int participantsNumber = new Random().nextInt(maxParticipants) + 1;
                auction.add_bids(Utils.generate_normal_bids(coinType, participantsNumber, pr[i], r[i]));
                i++;
            }
            //out.println("total: " + participantsNumber*2 + "\n");
            // Generating new random bids with unspent coins from previous auction
//            auction.add_bids(Utils.generate_bids(unspent_coins));
            //out.println("Coins ages of the reused bids:");
            /*int a = 0, b = 0;
            for(Bid bid : lost_bids){
                if(auction.getCoinAge(bid).get().age == (maxCoinAge-1) && bid.id.equals("honest participant"))
                    a++;
                if(auction.getCoinAge(bid).get().age == (maxCoinAge-1) && bid.id.equals("strategic participant"))
                    b++;
            }*/
            //out.println(participantsNumber*2 + " " + a + " " + b);
            //lost_bids.forEach(bid -> auction.getCoinAge(bid).ifPresent(ca -> out.println(bid.id + " : " + ca.age)));
            // Update price of lost bids by specified percent
            auction.add_bids(Utils.update_bids_price(lost_bids, 0.01));

            auction.select_winners();
            if(verbose){
                out.print(auction.toString());
            }
            prevAuction = Optional.of(auction);
            // Setting new list of unspent coins according to the current auction results
            unspent_coins = new ArrayList<>(auction.unspent_coins());
            lost_bids = auction.getSpendableLostBids();
        }
        return true;
    }

    public static boolean run(boolean verbose){
        try {
            return test(verbose);
        } catch (Exception e){
            if(verbose){
                out.println("Exception: " + e.getMessage());
            }
            return false;
        }
    }
}