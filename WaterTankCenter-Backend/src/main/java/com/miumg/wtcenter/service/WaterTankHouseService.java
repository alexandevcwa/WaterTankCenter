package com.miumg.wtcenter.service;

import com.miumg.wtcenter.dto.HouseDto;
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

    private static int housesRegistered = 0;

    public void addHouse(HouseDto houseDto) {
        housesRegistered++;
        houseDto.setId(housesRegistered);
        executor.submit(new WaterTankHouseLink(houseDto, wtcl, messagingTemplate));
    }
}
