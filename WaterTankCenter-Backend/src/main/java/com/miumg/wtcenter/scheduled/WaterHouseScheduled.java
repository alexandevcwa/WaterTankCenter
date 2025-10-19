package com.miumg.wtcenter.scheduled;

import com.miumg.wtcenter.dto.HouseDto;
import com.miumg.wtcenter.service.WaterDistributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WaterHouseScheduled {

    private final WaterDistributionService waterDistributionService;
    private static int houses = 0;


    @Scheduled(fixedRate = 20000)
    public void executeClient(){
        if(houses% 2 == 0){
            waterDistributionService.createHouse(new HouseDto(1.5,10));
        } else{
            waterDistributionService.createHouse(new HouseDto(3,6));
        }

    }

}
