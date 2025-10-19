package com.miumg.wtcenter.simulink;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public class HouseLink implements Runnable{
    /**
     * The unique identifier for the house.
     */
    private final String id;

    /**
     * The water consumption rate in cubic meters per second.
     */
    private final double m3PerSecond;

    /**
     * The period in milliseconds at which the house consumes water.
     */
    private final long period;

    /**
     * The tank from which the house draws water.
     */
    private final TankLink tankLink;

    /**
     * The current consumption of the house.
     */
    private double consumption;

    /**
     * The timestamp of the last consumption event.
     */
    private boolean isConsuming;

    /**
     * The status of the house (e.g., "CONSUMING", "NO CONSUMING").
     */
    private String status;

    /**
     * Runs the water consumption process for the house.
     */
    @Override
    public void run() {
        try {
            log.info("Starting house link: {}", id);
            for (int i = 1; i <= period; i++) {
                if(tankLink.getCurrentVolume() <= 0  || tankLink.getMinimumVolume() < consumption){
                    log.info("Skipping house link: {}", id);
                    status = "NO CONSUMING";
                    isConsuming = false;
                    i--;
                }
                tankLink.consumeWater(m3PerSecond);
                consumption += m3PerSecond;
                log.info("House {} consumed {} m3 of water. Tank volume: {} m3", id, consumption, tankLink.getCurrentVolume());
                status = "CONSUMING";
                isConsuming = true;
                Thread.sleep(1000);
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }
}
