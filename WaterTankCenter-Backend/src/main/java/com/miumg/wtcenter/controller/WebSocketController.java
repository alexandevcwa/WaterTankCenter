package com.miumg.wtcenter.controller;

import com.miumg.wtcenter.simulink.WaterTankCentralLink;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final WaterTankCentralLink waterTankCentralLink;

    @MessageMapping("/tank/{tankId}")
    @SendTo("/topic/{tankId}")
    public WaterTankCentralLink tank(@DestinationVariable String tankId){
        return waterTankCentralLink;
    }

}
