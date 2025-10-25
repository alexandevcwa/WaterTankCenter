package com.miumg.wtcenter.service;

import com.miumg.wtcenter.dto.WaterTankDto;
import com.miumg.wtcenter.simulink.WaterTankCentralLink;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaterTankService {

    private final WaterTankCentralLink wtcl;
    private final SimpMessagingTemplate simpMessagingTemplate;

    /**
     * Sends the current status of the water tank to a predefined messaging topic using the WebSocket protocol.
     * <p>
     * The method uses the {@code SimpMessagingTemplate} to send a {@link WaterTankDto} object containing
     * the following tank information:
     * - Tank ID (hardcoded to "T1").
     * - Minimum water volume of the tank.
     * - Maximum water volume of the tank.
     * - Current water volume of the tank.
     * <p>
     * The message is sent to the topic "/topic/tank-status". This allows subscribers of the topic
     * to receive real-time updates about the tank's status, facilitating monitoring or alerting.
     * <p>
     * The method gathers the above information through the injected {@link WaterTankCentralLink} instance,
     * ensuring that the data reflects the latest state of the tank.
     */
    public void emitTankStatus() {
        simpMessagingTemplate.convertAndSend("/topic/tank-status",
                WaterTankDto.builder()
                        .minimumVolume(wtcl.getMinimumVolume())
                        .maximumVolume(wtcl.getMaximumVolume())
                        .currentVolume(wtcl.getCurrentVolume())
                        .radio(wtcl.getRadio())
                        .height(wtcl.getHight())
                        .consumo(WaterTankCentralLink.getConsumo())
                        .build()
        );

        log.debug("Tank status emitted via websocket");
    }
}
