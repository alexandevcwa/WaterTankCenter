package com.miumg.wtcenter.simulink;

import com.miumg.wtcenter.dto.HouseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Simula el flujo de agua desde la tubería hacia un tanque.
 * Calcula el caudal (Q) y el volumen acumulado (V) en función del tiempo y presión.
 * Incluye un control de llenado y vaciado automático del tanque.
 */
@Slf4j
@RequiredArgsConstructor
public class WaterTankHouseLink implements Runnable {

    private final HouseDto houseDto;
    private final WaterTankCentralLink waterTankCentralLink;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Calcula el volumen máximo del tanque basado en la geometría del tubo (m³).
     */
    private double vMax() {
        return Math.PI * Math.pow(houseDto.getWaterTank().getRadio(),2) * houseDto.getWaterPipe().getLength();
    }

    @Override
    public void run() {
        final double D = houseDto.getWaterPipe().getDiameter();
        final double L = houseDto.getWaterPipe().getLength();
        final double f = houseDto.getWaterPipe().getFrictionFactor();
        final double rho = houseDto.getWaterPipe().getWaterDensity();
        final double A = houseDto.getWaterPipe().getCrossSectionArea();

        final double Vmax = vMax();          // Volumen máximo (m³)
        final double Vmin = Vmax * 0.25;     // Umbral mínimo (25%)
        final double Voke = Vmax * 0.95;     // Umbral de llenado (95%)

        // Función de presión en función del tiempo
        UnivariateFunction deltaP = t -> Math.max(50000, 200000.0 - 8000.0 * t); // Nunca baja de 50 kPa

        // Caudal instantáneo Q(t): Bernoulli con pérdidas por fricción
        UnivariateFunction Q = t -> {
            double dp = deltaP.value(t);
            return A * Math.sqrt((2 * dp * D) / (rho * f * L)); // m³/s
        };

        double volumenAcumulado = 0;
        double tiempo = 0.0;
        double deltaT = 5.0; // Paso de simulación (s)

        while (true) {
            try {
                Thread.sleep((long) (deltaT * 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Simulación interrumpida");
                break;
            }

            // Control lógico: activar/desactivar llenado
            if((volumenAcumulado >= Vmin && volumenAcumulado <= Voke) || volumenAcumulado >= Vmax){
                continue;
            }

            // Nivel de agua en tanque central (porcentaje)
            double centralWaterPercent = (waterTankCentralLink.getCurrentVolume() / waterTankCentralLink.getMaximumVolume()) * 100.0;

            // Solo intentar extraer agua si hay suficiente
            if (centralWaterPercent <= 20.0) {
                continue;
            }

            // Avanza tiempo siempre
            tiempo += deltaT;

            // Caudal real solo si estamos llenando
            double q = Q.value(tiempo);

            // Incremento de volumen en este paso
            double incremento = q * deltaT;

            // Extraer agua del tanque central
            waterTankCentralLink.consumeWater(incremento);

            // Actualiza volumen acumulado en tanque de la casa
            volumenAcumulado = Math.max(0.0, Math.min(Vmax, volumenAcumulado + incremento));
            double porcentaje = (volumenAcumulado / Vmax) * 100.0;

            houseDto.getWaterTank().setCurrentVolume(volumenAcumulado);
            houseDto.getWaterTank().setCurrentPercentage(porcentaje);

            messagingTemplate.convertAndSend("/topic/house-water-tank", houseDto);
        }
    }
}
