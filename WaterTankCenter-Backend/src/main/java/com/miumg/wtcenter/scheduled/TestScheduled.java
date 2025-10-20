package com.miumg.wtcenter.scheduled;

import com.miumg.wtcenter.dto.HouseDto;
import com.miumg.wtcenter.dto.WaterPipeDto;
import com.miumg.wtcenter.dto.WaterTankDto;
import com.miumg.wtcenter.service.WaterTankHouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestScheduled {

    private final WaterTankHouseService waterTankHouseService;

    @Scheduled(fixedRate = 1000000000)
    public void test() {
        HouseDto houseDto = new HouseDto();
        houseDto.setWaterTank(WaterTankDto.builder()
                        .radio(1.5)
                        .minimumVolume(0.1)
                        .maximumVolume(4)
                        .diameter(1.2)
                        .currentPercentage(0)
                        .currentVolume(0)
                .build());

        houseDto.setWaterPipe(new WaterPipeDto(0.02,2));
        waterTankHouseService.addHouse(houseDto);
    }
}
