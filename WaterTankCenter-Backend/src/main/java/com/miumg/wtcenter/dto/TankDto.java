package com.miumg.wtcenter.dto;

public record TankDto(
        String id,
        double minimumVolume,
        double maximumVolume,
        double currentVolume,
        double radio,
        double hight
) {
}
