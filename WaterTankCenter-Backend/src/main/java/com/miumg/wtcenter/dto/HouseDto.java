package com.miumg.wtcenter.dto;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HouseDto {

    /**
     * Code of the house that will be monitored
     */
    Integer id;

    /**
     * The water tank of the house
     */
    @Valid
    WaterTankDto waterTank;

    /**
     * The water pipe connected from the water counter to the water tank
     */
    @Valid
    WaterPipeDto waterPipe;
}