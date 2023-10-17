package com.iohk;

import com.iohk.tests.ExtraFundingTest;
import com.iohk.tests.RandomBidsTest;
import com.iohk.tests.NormalDistributionTest;

import java.util.Random;
import java.util.stream.Collectors;

import static java.lang.Math.*;
import static java.lang.System.out;

public class Main {

    public static void main(String[] args) {
        out.println("RandomBidsTest:   " + (RandomBidsTest.run(true) ? "OK" : "Failed"));
        //out.println("ExtraFundingTest: " + (ExtraFundingTest.run(true) ? "OK" : "Failed"));
        //out.println("NormalDistributionTest: " + (NormalDistributionTest.run(true) ? "OK" : "Failed"));
    }
}