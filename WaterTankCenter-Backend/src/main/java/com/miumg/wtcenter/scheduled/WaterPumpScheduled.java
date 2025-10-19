package com.miumg.wtcenter.scheduled;

import com.miumg.wtcenter.service.WaterPumpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaterPumpScheduled {

    private final WaterPumpService waterPumpService;

    /**
     * Scheduled task that automatically triggers the water pump service to refill the tank
     * if the water volume is below the defined minimum threshold. This method is executed
     * at a fixed interval of 5000 milliseconds (5 seconds).
     * <p>
     * Delegates the refill operation to the {@code WaterPumpService#autoFillIfLow} method,
     * which manages the refilling logic, including monitoring and adjusting the water level
     * in the tank. The scheduling ensures periodic checks for maintaining sufficient water levels.
     * <p>
     * Thread Safety: The underlying {@code WaterPumpService#autoFillIfLow} method ensures
     * thread-safe execution by handling potential concurrent operations of the pump.
     */
    @Scheduled(fixedRate = 5000)
    public void autoFillIfLowScheduled() {
        waterPumpService.autoFillIfLow();
    }
}
