package com.miumg.wtcenter.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a pipe with a specified diameter and length.
 */
public class WaterPipeDto {
    /**
     * The diameter of the pipe in meters.
     */
    @Getter
    @NotNull(message = "El diámetro del tubo es requerido")
    private double diameter;

    /**
     * The length of the pipe in meters.
     */
    @Getter
    @NotNull(message = "La longitud del tubo es requerida")
    private final double length;

    /**
     * The density of water in kg/m^3.
     */
    @Getter @Setter
    private double waterDensity = 1000;

    /**
     * The friction factor of the pipe.
     */
    @Getter @Setter
    private double frictionFactor = 0.03;

    /**
     * Constructs a new PipeDto instance with the specified diameter and length.
     * @param diameter the diameter of the pipe in meters
     * @param length the length of the pipe in meters
     */
    public WaterPipeDto(double diameter, double length) {
        this.diameter = diameter;
        this.length = length;
    }

    /**
     * The area of the pipe in m^2.
     */
    public double getArea(){
        return Math.PI * Math.pow(diameter,2) / 4.0;
    }

    /**
     * The cross-section area of the pipe in m^2.
     * @return the cross-section area of the pipe in m^2
     */
    public double getCrossSectionArea(){
        return Math.PI * Math.pow(diameter / 2.0, 2);
    }
}
