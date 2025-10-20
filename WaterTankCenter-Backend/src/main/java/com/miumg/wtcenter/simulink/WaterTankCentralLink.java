package com.miumg.wtcenter.simulink;

import lombok.Getter;
import lombok.Synchronized;

/**
 * Represents a water tank with a maximum and minimum volume.
 */
public class WaterTankCentralLink {

    /**
     * The maximum volume of the tank.
     */
    @Getter
    private final double maximumVolume;

    /**
     * The minimum volume of the tank.
     */
    @Getter
    private final double minimumVolume;

    /**
     * The current volume of water in the tank.
     */
    private static double currentVolume;

    /**
     * The radio of the tank.
     */
    @Getter
    private final double radio;

    /**
     * The height of the tank.
     */
    @Getter
    private final double hight;


    /**
     * Constructs a new TankLink instance with the specified maximum volume and minimum percentage of the volume.
     *
     * @param radio the radio of the tank, in meters
     * @param hight the height of the tank, in meters
     * @param minimumPercentage the minimum percentage of the maximum volume allowed; must be between 0 and 100
     * @throws RuntimeException if the maximum volume is less than or equal to zero
     * @throws RuntimeException if the minimum percentage is outside the range [0, 100]
     */
    public WaterTankCentralLink(double radio, double hight, double minimumPercentage) {
        if (radio <= 0) throw new RuntimeException("Radio must be greater than zero");
        if (minimumPercentage < 0 || minimumPercentage > 100)
            throw new RuntimeException("Minimum percentage must be between 0 and 100");
        this.radio  = radio;
        this.hight = hight;
        this.maximumVolume = (Math.PI * radio) * hight;
        this.minimumVolume = minimumPercentage;
        currentVolume = 0;
    }

    /**
     * Retrieves the current volume of water in the tank.
     *
     * @return the current volume of water in the tank as a double
     */
    @Synchronized
    public double getCurrentVolume() {
        return currentVolume;
    }

    /**
     * Reduces the current volume of water in the tank by the specified amount.
     *
     * @param consumedVolume the volume of water to consume from the tank, in cubic meters
     */
    @Synchronized
    public void consumeWater(double consumedVolume) {
        currentVolume = currentVolume - consumedVolume;
    }

    /**
     * Refills the tank by adding the specified volume of water to the current volume.
     *
     * @param refilledVolume the volume of water to add to the tank, in cubic meters
     */
    @Synchronized
    public void refillWater(double refilledVolume) {
        currentVolume = currentVolume + refilledVolume;
    }

    @Override
    public String toString() {
        return String.format("TankLink { Current=%.2f m³, Min=%.2f m³, Max=%.2f m³, Radio=%.2f m, Hight=%.2f m}",
                currentVolume, this.minimumVolume, this.maximumVolume,this.radio,this.hight);
    }
}
