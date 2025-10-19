package com.miumg.wtcenter.service;

import com.miumg.wtcenter.simulink.TankLink;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaterPumpService {

    /**
     * The volume of water the pump can input per second (in cubic meters).
     */
    @Value("${waterPump.inputVolumeThresholdInM3PerSecond}")
    private double inputVolumeThresholdInM3PerSecond;

    /**
     * Represents a link to a water tank used for monitoring and controlling
     * the tank's current volume, maximum volume, and minimum volume thresholds.
     * This instance is used to manage operations such as water refilling or
     * consumption in the context of water pump services.
     * <p>
     * The TankLink object provides synchronized methods to interact with
     * the tank's current volume, ensuring thread-safe operations while managing water
     * levels. It is a core dependency for services controlling water distribution.
     */
    private final TankLink tankLink;

    /**
     * The volume threshold to start saving water (as a percentage of max volume).
     */
    private static final double SAVE_VOLUME_THRESHOLD_VOLUME = 0.96;

    /**
     * The volume threshold to start saving water (in cubic meters).
     */
    private static double saveVolumeThreshold;

    /**
     * Indicates whether the pump is currently running.
     */
    private static final AtomicBoolean pumping = new AtomicBoolean(false);

    @PostConstruct
    public void postConstruct() {
        saveVolumeThreshold = tankLink.getMaximumVolume() * SAVE_VOLUME_THRESHOLD_VOLUME;
    }

    /**
     * Automatically triggers the water pump to refill the tank if the current water volume
     * is below or equal to the minimum volume threshold and the pump is not already running.
     * The refilling process continues until the current volume reaches a predefined
     * "safe volume threshold."
     * <p>
     * The method runs the refilling operation in a separate thread to ensure non-blocking
     * operation. During the process, it logs the current tank volume at regular intervals
     * and refills the tank with a specified volume per second. If the thread is interrupted,
     * it handles the interruption and ensures the pump state is properly reset.
     * <p>
     * Preconditions:
     * - The tank volume is monitored via {@code tankLink.getCurrentVolume()}.
     * - The minimum allowable tank volume is obtained via {@code tankLink.getMinimumVolume()}.
     * - Refilling operations are handled by {@code tankLink.refillWater(double)}.
     * <p>
     * Thread Safety:
     * - The method ensures thread-safe operations using an {@code AtomicBoolean} indicator
     * to determine whether the pump is actively running, preventing simultaneous pump operations.
     */
    public void autoFillIfLow() {
        if (tankLink.getCurrentVolume() <= tankLink.getMinimumVolume() && !pumping.get()) {
            new Thread(() -> {
                log.debug("Starting water filling pump");
                try {
                    pumping.set(true);
                    do {
                        log.debug(tankLink.toString());
                        tankLink.refillWater(inputVolumeThresholdInM3PerSecond);
                        Thread.sleep(1000);
                    } while (tankLink.getCurrentVolume() < saveVolumeThreshold);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    pumping.set(false);
                }
            }).start();
        }
    }
}
