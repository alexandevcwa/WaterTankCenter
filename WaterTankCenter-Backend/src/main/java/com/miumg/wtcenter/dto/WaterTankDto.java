package com.miumg.wtcenter.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WaterTankDto {

    int id;

    double minimumVolume;

    double maximumVolume;

    double currentVolume;

    double currentPercentage;

    double radio;

    @NotNull(message = "El alto del tanque es requerido")
    double height;

    @NotNull(message = "El diámetro del tanque es requerido")
    double diameter;

    boolean filling;
}