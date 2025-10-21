package com.miumg.wtcenter.service;

import com.miumg.wtcenter.common.Pipes;
import com.miumg.wtcenter.common.UnitsConverter;
import com.miumg.wtcenter.dto.*;
import com.miumg.wtcenter.simulink.WaterTankCentralLink;
import com.miumg.wtcenter.simulink.WaterTankHouseLink;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class WaterTankHouseService {

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final SimpMessagingTemplate messagingTemplate;
    private final WaterTankCentralLink wtcl;

    private static int registered = 0;

    public void addHouse(WaterTankRequestDto request) {
        double pipeDiameter = UnitsConverter.fromInchesToMeters(Pipes.pipes.get(request.pipeDiameter()));
        WaterPipeDto pipe = new WaterPipeDto(pipeDiameter, request.pipeLength());

        WaterTankDto tank = WaterTankDto.builder()
                .height(request.tankHeight())
                .diameter(request.tankDiameter())
                .build();

        registered++;

        HouseDto house = new HouseDto(registered, tank, pipe);

        executor.submit(new WaterTankHouseLink(house, wtcl, messagingTemplate));
    }
}
