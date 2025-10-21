package com.miumg.wtcenter.common;

public class UnitsConverter {
    public static double fromInchesToCentimeters(double inches) {
        return inches * 2.54;
    }

    public static double fromInchesToMeters(double inches) {
        return inches * 2.54 / 100;
    }

}
