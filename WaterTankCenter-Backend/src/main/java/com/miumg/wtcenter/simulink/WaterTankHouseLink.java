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
    private static final double MIN_LEVEL_RATIO = 0.25;   // 25% del volumen mínimo
    private static final double FULL_LEVEL_RATIO = 0.92;  // 92% del volumen de seguridad
    private static final double MIN_PRESSURE = 50_000.0;  // Presión mínima (Pa)
    private static final double INITIAL_PRESSURE = 150_000.0;
    private static final double PRESSURE_DROP_RATE = 400.0; // Pa por segundo (ajusta)


    private final HouseDto houseDto;
    private final WaterTankCentralLink waterTankCentralLink;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void run() {
        final double vMax = calculateMaxVolume();
        final double vMin = vMax * MIN_LEVEL_RATIO;
        final double vOk = vMax * FULL_LEVEL_RATIO;

        houseDto.getWaterTank().setMaximumVolume(vMax);
        houseDto.getWaterTank().setMinimumVolume(vMin);
        houseDto.getWaterTank().setSaveVolume(vOk);
        houseDto.getWaterTank().setFilling(false);

        UnivariateFunction pressureFunction = createPressureFunction(2.0);
        UnivariateFunction flowFunction = createFlowFunction(pressureFunction);

        double volume = 0.0;
        double time = 0.0;

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep((long) (DELTA_T * 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            if (shouldPauseFilling(volume, vMin, vOk, vMax)) continue;
            if (!hasEnoughCentralWater()) continue;

            time += DELTA_T;
            double rawIncrement = flowFunction.value(time) * DELTA_T;
            double remainingSpace = Math.max(0.0, vMax - volume);
            double availableCentral = Math.max(0.0, waterTankCentralLink.getCurrentVolume());

            // toma el menor: lo que fluye, lo que cabe y lo que hay
            double actualIncrement = Math.min(rawIncrement, Math.min(remainingSpace, availableCentral));
            if (actualIncrement <= 0.0) {
                houseDto.getWaterTank().setFilling(false);
                notifyClients();
                continue;
            }

            volume = updateWaterVolumes(actualIncrement, vMax);
            notifyClients();
        }
    }

    // ----------------------------
    //        MÉTODOS PRIVADOS
    // ----------------------------

    /**
     * Calcula el volumen máximo del tanque (m³).
     */
    private double calculateMaxVolume() {
        double radius = houseDto.getWaterTank().getDiameter() /2;
        houseDto.getWaterTank().setRadio(radius);

        double length = houseDto.getWaterTank().getHeight();
        return Math.PI * Math.pow(radius, 2) * length;
    }

    private static final double G = 9.80665;
    /**
     * Crea una función que describe la presión en función del tiempo.
     */
    private UnivariateFunction createPressureFunction(double maxHeadMeters) {
        final double maxDp = houseDto.getWaterPipe().getWaterDensity() * G * maxHeadMeters;
        return t -> {
            double raw = INITIAL_PRESSURE - PRESSURE_DROP_RATE * t;
            double dp = Math.max(MIN_PRESSURE, raw);
            return Math.min(dp, maxDp); // limita a presión correspondiente a maxHeadMeters
        };
    }



    /**
     * Crea una función de caudal instantáneo Q(t) considerando pérdidas por fricción.
     */
    private UnivariateFunction createFlowFunction(UnivariateFunction deltaP) {
        double D = houseDto.getWaterPipe().getDiameter();       // m
        double L = houseDto.getWaterPipe().getLength();         // m
        double f = houseDto.getWaterPipe().getFrictionFactor(); // Darcy
        double rho = houseDto.getWaterPipe().getWaterDensity(); // kg/m^3
        double A = houseDto.getWaterPipe().getCrossSectionArea(); // m^2

        double finalD = (D > 0.0) ? D : (A > 0.0 ? Math.sqrt((4.0 * A) / Math.PI) : 0.0);
        double finalA = (A > 0.0) ? A : (finalD > 0.0 ? Math.PI * Math.pow(finalD, 2) / 4.0 : 0.0);
        double finalL = (L > 0.0) ? L : 1.0;
        double finalF = (f > 0.0) ? f : 0.02;
        double finalRho = (rho > 0.0) ? rho : 1000.0;

        final double eps = 1e-12;

        return t -> {
            double dp = deltaP.value(t); // *DEBE* ser Δp (Pa)
            if (Double.isNaN(dp) || Double.isInfinite(dp) || dp <= 0.0) return 0.0;
            if (finalA <= eps || finalD <= eps || finalL <= eps || finalF <= eps || finalRho <= eps) {
                log.warn("Parámetros inválidos D={}, A={}, L={}, f={}, rho={}", finalD, finalA, finalL, finalF, finalRho);
                return 0.0;
            }
            double v = Math.sqrt((2.0 * dp * finalD) / (finalRho * finalF * finalL));
            if (Double.isNaN(v) || Double.isInfinite(v) || v <= 0.0) return 0.0;
            double q = finalA * v;
            return q;
        };
    }



    /**
     * Determina si debe pausar el llenado según los umbrales de volumen.
     */
    private boolean shouldPauseFilling(double volume, double vMin, double vOk, double vMax) {
        return (volume > vOk);
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
    private double updateWaterVolumes(double increment, double vMax) {
        waterTankCentralLink.consumeWater(increment);

        double newVolume = Math.max(0.0,
                Math.min(vMax, houseDto.getWaterTank().getCurrentVolume() + increment));
        houseDto.getWaterTank().setCurrentVolume(newVolume);
        houseDto.getWaterTank().setCurrentPercentage((newVolume / vMax) * 100.0);

        houseDto.getWaterTank().setFilling(!(newVolume >= houseDto.getWaterTank().getSaveVolume()));

        return newVolume;
    }

    /**
     * Envía el estado actual del tanque al cliente mediante WebSocket.
     */
    private void notifyClients() {
        messagingTemplate.convertAndSend("/topic/house-water-tank", houseDto);
    }
}
