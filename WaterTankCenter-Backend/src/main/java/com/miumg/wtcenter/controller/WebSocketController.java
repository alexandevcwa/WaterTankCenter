package com.miumg.wtcenter.controller;

import com.miumg.wtcenter.simulink.TankLink;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final TankLink tankLink;

    @MessageMapping("/tank/{tankId}")
    @SendTo("/topic/{tankId}")
    public TankLink tank(@DestinationVariable String tankId){
        return tankLink;
    }

}
