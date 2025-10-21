package com.miumg.wtcenter.common;

import java.util.Map;

public class Pipes {

    public static Map<String, Double> pipes = Map.ofEntries(
            Map.entry("INCH_1_8", 0.125),
            Map.entry("INCH_1_4", 0.25),
            Map.entry("INCH_3_8", 0.375),
            Map.entry("INCH_1_2", 0.5),
            Map.entry("INCH_3_4", 0.75),
            Map.entry("INCH_1", 1.0),
            Map.entry("INCH_1_1_4", 1.25),
            Map.entry("INCH_1_1_2", 1.5),
            Map.entry("INCH_2", 2.0),
            Map.entry("INCH_2_5", 2.5),
            Map.entry("INCH_3", 3.0),
            Map.entry("INCH_4", 4.0),
            Map.entry("INCH_6", 6.0),
            Map.entry("INCH_8", 8.0),
            Map.entry("INCH_10", 10.0),
            Map.entry("INCH_12", 12.0)
    );

    public static double toCentimeters(double inches) {
        return inches * 2.54;
    }

    public static double toMeters(double inches) {
        return inches * 2.54 / 100;
    }
}
