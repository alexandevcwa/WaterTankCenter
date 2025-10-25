package com.miumg.wtcenter.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WaterTankDto {

    /**
     * Minimum volumen in cubic meters.
     */
    double minimumVolume;

    /**
     * Maximum volumen in cubic meters.
     */
    double maximumVolume;

    /**
     * Volumen to save in cubic meters.
     */
    double saveVolume;

    /**
     * Current volumen in cubic meters.
     */
    double currentVolume;

    /**
     * Current percentage of the tank.
     */
    double currentPercentage;

    /**
     * The radio of the tank in meters.
     */
    double radio;

    /**
     * The height of the tank in meters.
     */
    @NotNull(message = "El alto del tanque es requerido")
    double height;

    /**
     * The diameter of the tank in meters.
     */
    @NotNull(message = "El diámetro del tanque es requerido")
    double diameter;

    /**
     * Tag that indicated if the tank is filling.
     */
    boolean isFilling;

    /**
     * Consumo actual del tanque
     */
    double consumo;

    double tiempoSegundos;
}