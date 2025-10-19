package com.miumg.wtcenter.scheduled;

import com.miumg.wtcenter.service.WaterTankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaterTankScheduled {

    private final WaterTankService waterTankService;

    /**
     * Scheduled task responsible for monitoring the status of the water tank at fixed intervals.
     * This method is executed every 5000 milliseconds (5 seconds) and delegates the tank status
     * emission to the {@code WaterTankService#emitTankStatus} method, which broadcasts the current
     * state of the water tank.
     * <p>
     * The broadcasted status includes details such as the tank's ID, minimum volume, maximum volume,
     * and current volume. This information can be used for real-time monitoring and decision-making
     * regarding the water tank's state.
     * <p>
     * Thread Safety:
     * The underlying {@code emitTankStatus} method ensures safe execution of the broadcasting
     * process, including accessing and sending information regarding the tank's current state.
     */
    @Scheduled(fixedRate = 5000)
    public void checkWaterTank() {
        log.debug("Starting scheduled to emit tank status");
        waterTankService.emitTankStatus();
    }
}
