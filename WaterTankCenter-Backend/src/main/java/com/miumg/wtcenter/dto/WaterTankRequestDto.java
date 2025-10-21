package com.miumg.wtcenter.dto;

public record WaterTankRequestDto(
        double tankHeight,
        double tankDiameter,
        double pipeLength,
        String pipeDiameter
) {
}
