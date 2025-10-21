package com.miumg.wtcenter.dto;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class HouseDto {

    /**
     * Code of the house that will be monitored
     */
    final Integer id;

    /**
     * The water tank of the house
     */
    @Valid
    final WaterTankDto waterTank;

    /**
     * The water pipe connected from the water counter to the water tank
     */
    @Valid
    final WaterPipeDto waterPipe;
}