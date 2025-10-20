package com.miumg.wtcenter.simulink;

import com.miumg.wtcenter.dto.HouseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Simula el flujo de agua desde la tubería hacia un tanque doméstico.
 * Calcula el caudal (Q) y el volumen acumulado (V) en función del tiempo y presión.
 * Incluye control de llenado/vaciado automático del tanque según umbrales.
 */
@Slf4j
@RequiredArgsConstructor
public class WaterTankHouseLink implements Runnable {

    private static final double DELTA_T = 5.0;      // Paso de simulación (segundos)
    private static final double MIN_CENTRAL_LEVEL = 20.0; // % mínimo del tanque central para permitir extracción
    private static final double MIN_LEVEL_RATIO = 0.25;   // 25% del volumen máximo
    private static final double FULL_LEVEL_RATIO = 0.95;  // 95% del volumen máximo
    private static final double MIN_PRESSURE = 50_000.0;  // Presión mínima (Pa)
    private static final double INITIAL_PRESSURE = 200_000.0;
    private static final double PRESSURE_DROP_RATE = 8_000.0; // Disminución de presión por segundo

    private final HouseDto houseDto;
    private final WaterTankCentralLink waterTankCentralLink;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void run() {
        final double vMax = calculateMaxVolume();
        final double vMin = vMax * MIN_LEVEL_RATIO;
        final double vOk = vMax * FULL_LEVEL_RATIO;

        UnivariateFunction pressureFunction = createPressureFunction();
        UnivariateFunction flowFunction = createFlowFunction(pressureFunction);

        double volume = 0.0;
        double time = 0.0;

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep((long) (DELTA_T * 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Simulación interrumpida para la casa {}", houseDto.getId());
                break;
            }

            if (shouldPauseFilling(volume, vMin, vOk, vMax)) continue;
            if (!hasEnoughCentralWater()) continue;

            time += DELTA_T;
            double increment = flowFunction.value(time) * DELTA_T;

            updateWaterVolumes(increment, vMax);
            notifyClients();
        }

        log.info("Finalizó la simulación del tanque para la casa: {}", houseDto.getId());
    }

    // ----------------------------
    //        MÉTODOS PRIVADOS
    // ----------------------------

    /**
     * Calcula el volumen máximo del tanque (m³).
     */
    private double calculateMaxVolume() {
        double radius = houseDto.getWaterTank().getRadio();
        double length = houseDto.getWaterPipe().getLength();
        return Math.PI * Math.pow(radius, 2) * length;
    }

    /**
     * Crea una función que describe la presión en función del tiempo.
     */
    private UnivariateFunction createPressureFunction() {
        return t -> Math.max(MIN_PRESSURE, INITIAL_PRESSURE - PRESSURE_DROP_RATE * t);
    }

    /**
     * Crea una función de caudal instantáneo Q(t) considerando pérdidas por fricción.
     */
    private UnivariateFunction createFlowFunction(UnivariateFunction deltaP) {
        double D = houseDto.getWaterPipe().getDiameter();
        double L = houseDto.getWaterPipe().getLength();
        double f = houseDto.getWaterPipe().getFrictionFactor();
        double rho = houseDto.getWaterPipe().getWaterDensity();
        double A = houseDto.getWaterPipe().getCrossSectionArea();

        return t -> {
            double dp = deltaP.value(t);
            return A * Math.sqrt((2 * dp * D) / (rho * f * L)); // m³/s
        };
    }

    /**
     * Determina si debe pausar el llenado según los umbrales de volumen.
     */
    private boolean shouldPauseFilling(double volume, double vMin, double vOk, double vMax) {
        return (volume >= vMin && volume <= vOk) || volume >= vMax;
    }

    /**
     * Verifica si el tanque central tiene suficiente agua para continuar.
     */
    private boolean hasEnoughCentralWater() {
        double centralPercent = (waterTankCentralLink.getCurrentVolume() /
                waterTankCentralLink.getMaximumVolume()) * 100.0;
        return centralPercent > MIN_CENTRAL_LEVEL;
    }

    /**
     * Actualiza el volumen actual en el tanque de la casa y el tanque central.
     */
    private void updateWaterVolumes(double increment, double vMax) {
        waterTankCentralLink.consumeWater(increment);

        double newVolume = Math.max(0.0,
                Math.min(vMax, houseDto.getWaterTank().getCurrentVolume() + increment));

        houseDto.getWaterTank().setCurrentVolume(newVolume);
        houseDto.getWaterTank().setCurrentPercentage((newVolume / vMax) * 100.0);
    }

    /**
     * Envía el estado actual del tanque al cliente mediante WebSocket.
     */
    private void notifyClients() {
        messagingTemplate.convertAndSend("/topic/house-water-tank", houseDto);
    }
}
