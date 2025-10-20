package com.miumg.wtcenter.common;

public class Pipes {
    public static final double INCH_1_8   = 0.125;
    public static final double INCH_1_4   = 0.25;
    public static final double INCH_3_8   = 0.375;
    public static final double INCH_1_2   = 0.5;
    public static final double INCH_3_4   = 0.75;
    public static final double INCH_1     = 1.0;
    public static final double INCH_1_1_4 = 1.25;
    public static final double INCH_1_1_2 = 1.5;
    public static final double INCH_2     = 2.0;
    public static final double INCH_2_5   = 2.5;
    public static final double INCH_3     = 3.0;
    public static final double INCH_4     = 4.0;
    public static final double INCH_6     = 6.0;
    public static final double INCH_8     = 8.0;
    public static final double INCH_10    = 10.0;
    public static final double INCH_12    = 12.0;

    public static double toCentimeters(double inches) {
        return inches * 2.54;
    }

    public static double toMeters(double inches) {
        return inches * 2.54 / 100;
    }
}
