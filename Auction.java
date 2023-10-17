package com.iohk;

import com.iohk.coin.CoinAge;
import com.iohk.coin.CoinPoB;
import com.iohk.coin.CoinType;
import com.iohk.criterions.ExtraFundingCriterion;
import com.iohk.prices.BasePrice;
import com.iohk.prices.ClearingPrice;
import com.iohk.prices.MedianPrice;

import java.util.*;
import java.util.stream.Collectors;

public class Auction {
    // Epoch number
    private final int epochNumber;

    // Fund initially available and spent
    private final double fund;
    private double spent = 0;

    // Auction constant parameters
    private final double fundRegularVolume;
    private final double fundExtraVolume;
    private final int maxCoinAge;

    // Auction initial parameters (are recalculated each new epoch)
    private final Map<CoinType, BasePrice> basePrices = new TreeMap<>();
    private final List<CoinPoB> coinsDiscarded = new ArrayList<>();
    private final List<CoinAge> coinsAges = new ArrayList<>();

    // Auction current data and results
    private List<Bid> allBids = new ArrayList<>();
    private List<Bid> winningBids = new ArrayList<>();
    private List<Bid> lostBids = new ArrayList<>();

    // Parameters calculated by auction results
    private Map<CoinType, MedianPrice> medianPricesWinning = new TreeMap<>();
    private Map<CoinType, MedianPrice> medianPricesLost = new TreeMap<>();
    private ExtraFundingCriterion extraFundingCriterion = new ExtraFundingCriterion();
    private List<ClearingPrice> clearingPrices = new ArrayList<>();

    public Auction(double fundRegularVolume,
                   double fundExtraVolume,
                   int maxCoinAge,
                   Map<CoinType, BasePrice> basePrices) {
        this.epochNumber = 0;

        this.fundRegularVolume = fundRegularVolume;
        this.fundExtraVolume = fundExtraVolume;
        this.maxCoinAge = maxCoinAge;

        this.fund = fundRegularVolume;
        this.basePrices.putAll(basePrices);
    }

    public Auction(Auction prev) throws Exception {
        if (!prev.is_finished()) {
            throw new Exception("Initialization with an unfinished auction");
        }
        this.epochNumber = prev.epochNumber + 1;
        this.fundRegularVolume = prev.fundRegularVolume;
        this.fundExtraVolume = prev.fundExtraVolume;
        this.maxCoinAge = prev.maxCoinAge;

        this.fund = prev.extraFundingCriterion.is_true() ? (prev.fund + fundExtraVolume) : fundRegularVolume;

        coinsDiscarded.addAll(prev.coinsDiscarded);
        for (Bid b : prev.winningBids) {
            coinsDiscarded.add(b.coinsBurnt);
        }

        for (Bid b : prev.lostBids) {
            // Adding age counter for a coin, that participated first time in a previous auction
            if (prev.coinsAges.stream().noneMatch(ca -> ca.coin == b.coinsBurnt)) {
                coinsAges.add(new CoinAge(b.coinsBurnt));
            }
        }

        for (CoinAge ca : prev.coinsAges) {
            // If coin won, the age counter for it is not needed more
            if ((prev.winningBids.stream().map(b -> b.coinsBurnt).noneMatch(c -> c == ca.coin))) {
                // Increment coin's age only if it participated in previous auction
                boolean coinParticipated = (prev.lostBids.stream().map(b -> b.coinsBurnt).anyMatch(c -> c == ca.coin));
                CoinAge updatedCoinAge = (coinParticipated ? ca.incremented() : ca);

                // Discard coin if it has reached the maximum age
                if (updatedCoinAge.age == maxCoinAge) {
                    coinsDiscarded.add(ca.coin);
                } else {
                    coinsAges.add(updatedCoinAge);
                }
            }
        }

        for (Map.Entry<CoinType, BasePrice> basePriceEntry : prev.basePrices.entrySet()) {
            CoinType coinType = basePriceEntry.getKey();
            BasePrice basePrice = basePriceEntry.getValue();
            basePrices.put(coinType, basePrice.updated(prev.winningBids, prev.medianPricesWinning.get(coinType).value));
        }
    }

    public void add_bids(List<Bid> newBids) throws Exception {
        for (Bid b : newBids) {
            if (!basePrices.containsKey(b.coinsBurnt.coinType)) {
                throw new Exception("Unsupported coin");
            }
            if (coinsDiscarded.contains(b.coinsBurnt)) {
                throw new Exception("Bid contains discarded coin");
            }
            b.set_relative_price(basePrices);
        }
        allBids.addAll(newBids);

        // Sort bids by relative price
        allBids = allBids.stream()
                .sorted((a, b) -> Double.compare(b.relative_price(), a.relative_price()))
                .collect(Collectors.toList());
    }

    public void select_winners() {
        // Winners are already selected
        if (is_finished()) {
            return;
        }

        // Selecting bids with the biggest relative prices until there are enough assets in the fund
        // Note: assuming the list of bids is already sorted
        for (Bid b : allBids) {
            if (lostBids.isEmpty() &&
                    spent + b.dustsToGet <= fund) {
                spent += b.dustsToGet;
                winningBids.add(b);
            } else {
                lostBids.add(b);
            }
        }

        // Calculating median prices by auction results
        for (Map.Entry<CoinType, BasePrice> basePriceEntry : basePrices.entrySet()) {
            CoinType coinType = basePriceEntry.getKey();
            medianPricesWinning.put(coinType, new MedianPrice(coinType, winningBids));
            medianPricesLost.put(coinType, new MedianPrice(coinType, lostBids));
        }

        // Initializing an extra funding criterion with auction results
        extraFundingCriterion = new ExtraFundingCriterion(basePrices.keySet(), winningBids, lostBids, medianPricesWinning, medianPricesLost);

        // Calculating clearing prices for each type of coin (price of a last bid by each coin type in a sorted list of winning bids)
        List<Bid> winningBidsReversed = new ArrayList<>(winningBids);
        Collections.reverse(winningBidsReversed);
        for(Bid bid : winningBidsReversed){
            CoinType coinType = bid.coinsBurnt.coinType;
            if(clearingPrices.stream().noneMatch(cp -> cp.coinType == coinType)){
                clearingPrices.add(new ClearingPrice(coinType, bid.price()));
            }
        }
        clearingPrices.sort(Comparator.comparing(cp -> cp.coinType)); // ordering clearing prices by the CoinType
    }

    public List<CoinPoB> unspent_coins()
    {
        List<CoinPoB> unspent_coins_all = lostBids.stream().map(b -> b.coinsBurnt).collect(Collectors.toList());
        List<CoinPoB> unspent_coins_unexpired = new ArrayList<CoinPoB>();

        // filtering the coins those age is expired
        for(CoinPoB coin : unspent_coins_all){
            List<CoinAge> coinAge = coinsAges.stream().filter(ca -> ca.coin == coin).collect(Collectors.toList());
            if(!coinAge.isEmpty() &&
                    coinAge.get(0).age + 1 >= maxCoinAge){ // coin reaches maximum age after the current epoch
                continue;
            }
            unspent_coins_unexpired.add(coin);
        }
        return unspent_coins_unexpired;
    }

    public List<Bid> getLostBids(){ return lostBids; }
    public List<Bid> getSpendableLostBids(){
        List<CoinPoB> unspent = unspent_coins();
        return lostBids.stream().filter(b -> unspent.contains(b.coinsBurnt)).collect(Collectors.toList());
    }

    // Returns age of a coin for a current epoch, i.e. the number of previous auctions, where the coin participated
    public Optional<CoinAge> getCoinAge(CoinPoB coin){
        List<CoinAge> age = coinsAges.stream().filter(ca -> ca.coin.pobIdentifier == coin.pobIdentifier).collect(Collectors.toList());
        return age.isEmpty() ? Optional.empty() : Optional.of(age.get(0));
    }
    public Optional<CoinAge> getCoinAge(Bid bid){
        return getCoinAge(bid.coinsBurnt);
    }

    public boolean is_finished(){ return (!winningBids.isEmpty()); }
    public boolean reserve_is_needed(){ return extraFundingCriterion.is_true(); }

    public String toString(){
        String basePricesStr = "";
        for(Map.Entry<CoinType, BasePrice> basePriceEntry : basePrices.entrySet()){
            basePricesStr += basePriceEntry.getValue().toString() + "\n";
        }
        String discardedCoinsStr = "";
        for(CoinPoB dc : coinsDiscarded){
            discardedCoinsStr += dc.toString() + "\n";
        }
        String coinsAgesStr = "";
        for(CoinAge ca : coinsAges){
            coinsAgesStr += ca.toString() + "\n";
        }
        String allBidsStr = "";
        for(Bid b : allBids){
            allBidsStr += b.toString() + "\n";
        }
        String winningBidsStr = "";
        for(Bid b : winningBids){
            winningBidsStr += b.toString() + "\n";
        }
        String lostBidsStr = "";
        for(Bid b : lostBids){
            lostBidsStr += b.toString() + "\n";
        }
        String medianPricesWinningStr = "";
        for(Map.Entry<CoinType, MedianPrice> medianPriceEntry : medianPricesWinning.entrySet()){
            medianPricesWinningStr += medianPriceEntry.getValue().toString() + " ";
        }
        String medianPricesLostStr = "";
        for(Map.Entry<CoinType, MedianPrice> medianPriceEntry : medianPricesLost.entrySet()){
            medianPricesLostStr += medianPriceEntry.getValue().toString() + "\n";
        }
        String clearingPricesStr = "";
        for(ClearingPrice clearingPrice : clearingPrices){
            clearingPricesStr += clearingPrice.toString() + " ";
        }
        return ("------------------------------------------------------------\n" +
                "Epoch: " + epochNumber +     "\n" +
                "------------------------------------------------------------\n" +
                "Fund: "  + fund +            "\n" +
                "------------------------------\n" +
                "Base prices:\n" +
                "------------------------------\n" +
                basePricesStr +
                "------------------------------\n" +
                "Discarded coins:\n" +
                "------------------------------\n" +
                discardedCoinsStr +
                "------------------------------\n" +
                "Coins ages:\n" +
                "------------------------------\n" +
                coinsAgesStr +
                "------------------------------\n" +
                "All bids:\n" +
                "------------------------------\n" +
                allBidsStr +
                "------------------------------\n" +
                "Winning bids:\n" +
                "------------------------------\n" +
                winningBidsStr +
                "------------------------------\n" +
                "Lost bids:\n" +
                "------------------------------\n" +
                lostBidsStr +
                "------------------------------\n" +
                "Median prices winning:\n" +
                "------------------------------\n" +
                medianPricesWinningStr +
                "------------------------------\n" +
                "Median prices lost:\n" +
                "------------------------------\n" +
                medianPricesLostStr +
                "------------------------------\n" +
                "Clearing prices:\n" +
                "------------------------------\n" +
                clearingPricesStr +
                "------------------------------\n" +
                "Dusts spent: "  + spent +  " of " + fund + "\n" +
                "------------------------------\n" +
                "Extra funding criterion: " + extraFundingCriterion.is_true() + "\n");
        //return(epochNumber + clearingPricesStr + medianPricesWinningStr + "\n");
    }
}