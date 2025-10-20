package com.miumg.wtcenter.dto;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HouseDto {

    Integer id;

    @Valid
    WaterTankDto waterTank;

    @Valid
    WaterPipeDto waterPipe;
}