package com.miumg.wtcenter.simulink;

import com.miumg.wtcenter.dto.HouseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Simulación de llenado del tanque de la casa desde un tanque central.
 * - Usa head limitado (metros) para calcular Δp realista.
 * - Usa ecuación de orificio: Q = Cd * A_orif * sqrt(2 * Δp / rho)
 * - Limita incremento por espacio disponible y agua disponible en central.
 */
@Slf4j
@RequiredArgsConstructor
public class WaterTankHouseLink implements Runnable {

    private static final double DELTA_T = 5.0;      // Paso de simulación (segundos)
    private static final double MIN_CENTRAL_LEVEL = 20.0; // % mínimo del tanque central para permitir extracción
    private static final double MIN_LEVEL_RATIO = 0.25;   // 25% del volumen mínimo
    private static final double FULL_LEVEL_RATIO = 0.92;  // 92% del volumen de seguridad

    // Presión / head (valores conservadores)
    private static final double MIN_PRESSURE = 5_000.0;   // Pa (5 kPa)
    private static final double INITIAL_PRESSURE = 20_000.0; // Pa (20 kPa) — usado sólo como raw antes del clamp
    private static final double DEFAULT_MAX_HEAD_METERS = 1.0; // m (puedes bajar a 0.5 si quieres menos flujo)

    // Parámetros del orificio/válvula para limitar caudal (configurables)
    private static final double ORIFICE_DIAMETER = 0.05; // m (50 mm). Cambia esto para limitar más o menos.
    private static final double ORIFICE_DISCHARGE_COEFF = 0.6; // Cd típico para orificio/válvula

    private static final double G = 9.80665; // gravedad

    private final HouseDto houseDto;
    private final WaterTankCentralLink waterTankCentralLink;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void run() {
        final double vMax = calculateMaxVolume();
        final double vMin = vMax * MIN_LEVEL_RATIO;
        final double vOk = vMax * FULL_LEVEL_RATIO;

        // Guardar en DTO
        houseDto.getWaterTank().setMaximumVolume(vMax);
        houseDto.getWaterTank().setMinimumVolume(vMin);
        houseDto.getWaterTank().setSaveVolume(vOk);
        houseDto.getWaterTank().setFilling(false);

        // Crear funciones: presión limitada por head y función de flujo basada en orificio
        UnivariateFunction pressureFunction = createPressureFunction(DEFAULT_MAX_HEAD_METERS);
        UnivariateFunction flowFunction = createFlowFunctionOrifice(pressureFunction);

        // Inicializar volumen desde DTO (importante)
        double volume = houseDto.getWaterTank().getCurrentVolume();
        double time = 0.0;

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep((long) (DELTA_T * 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            // Pausas y condiciones
            if (shouldPauseFilling(volume, vMin, vOk, vMax)) {
                // Notificar para mantener UI sincronizada
                notifyClients();
                continue;
            }
            if (!hasEnoughCentralWater()) {
                houseDto.getWaterTank().setFilling(false);
                notifyClients();
                continue;
            }

            time += DELTA_T;

            // Caudal instantáneo desde la función (m^3/s)
            double qInstant = flowFunction.value(time);
            if (Double.isNaN(qInstant) || Double.isInfinite(qInstant) || qInstant <= 0.0) {
                houseDto.getWaterTank().setFilling(false);
                notifyClients();
                continue;
            }

            // Volumen bruto que fluiría en este paso (m^3)
            double rawIncrement = qInstant * DELTA_T;

            // Espacio disponible y agua en central
            double remainingSpace = Math.max(0.0, vMax - volume);
            double availableCentral = Math.max(0.0, waterTankCentralLink.getCurrentVolume());

            // Tomar la mínima de las tres cantidades
            double actualIncrement = Math.min(rawIncrement, Math.min(remainingSpace, availableCentral));
            if (actualIncrement <= 0.0) {
                houseDto.getWaterTank().setFilling(false);
                notifyClients();
                continue;
            }

            // Consumir sólo la cantidad real transferida
            waterTankCentralLink.consumeWater(actualIncrement);

            // Actualizar tanque de casa
            double newVolume = Math.max(0.0, Math.min(vMax, volume + actualIncrement));
            houseDto.getWaterTank().setCurrentVolume(newVolume);
            houseDto.getWaterTank().setCurrentPercentage((newVolume / vMax) * 100.0);
            houseDto.getWaterTank().setFilling(newVolume < houseDto.getWaterTank().getSaveVolume());

            // Actualizar variable local para las siguientes iteraciones
            volume = newVolume;

            // Log para depuración. Ajusta nivel según necesites.
            log.debug("t={}s dp={}Pa Q={}m3/s rawInc={}m3 actualInc={}m3 remSpace={}m3 centralAvail={}m3 houseVol={}m3",
                    time, lastDpForDebug, qInstant, rawIncrement, actualIncrement, remainingSpace, availableCentral, volume);

            notifyClients();
        }
    }

    // ----------------------------
    //        MÉTODOS PRIVADOS
    // ----------------------------

    /**
     * Calcula el volumen máximo del tanque (m³), asumiendo forma cilíndrica
     */
    private double calculateMaxVolume() {
        double radius = houseDto.getWaterTank().getDiameter() / 2.0;
        houseDto.getWaterTank().setRadio(radius);
        double length = houseDto.getWaterTank().getHeight();
        return Math.PI * Math.pow(radius, 2) * length;
    }

    // variable de ayuda para debug (se actualiza en createPressureFunction)
    private volatile double lastDpForDebug = 0.0;

    /**
     * Crea una función de presión Δp(t) (Pa) basada en un head máximo (m).
     * Evita usar una presión absoluta sin sentido usando un clamp por head.
     */
    private UnivariateFunction createPressureFunction(double maxHeadMeters) {
        final double rho = (houseDto.getWaterPipe().getWaterDensity() > 0.0) ?
                houseDto.getWaterPipe().getWaterDensity() : 1000.0;
        final double maxDp = rho * G * Math.max(0.0, maxHeadMeters);

        return t -> {
            double raw = INITIAL_PRESSURE - 0.0 * t; // no decay por defecto; si quieres decaimiento ajusta aquí
            double dp = Math.max(MIN_PRESSURE, raw);
            dp = Math.min(dp, maxDp);
            lastDpForDebug = dp;
            return dp;
        };
    }

    /**
     * Crea la función de caudal usando la ecuación de orificio:
     * Q = Cd * A_orif * sqrt(2 * dp / rho)
     *
     * Se selecciona como área de orificio la mínima entre el área del pipe y el área del orificio configurado,
     * así si el orificio es más pequeño limita el flujo.
     */
    private UnivariateFunction createFlowFunctionOrifice(UnivariateFunction deltaP) {
        double Dpipe = houseDto.getWaterPipe().getDiameter();       // m (si está en otra unidad convierte antes)
        double Apipe = houseDto.getWaterPipe().getCrossSectionArea();
        double rho = (houseDto.getWaterPipe().getWaterDensity() > 0.0) ?
                houseDto.getWaterPipe().getWaterDensity() : 1000.0;

        // calcular área del orificio configurado
        double Aorifice = Math.PI * Math.pow(ORIFICE_DIAMETER, 2) / 4.0;

        // si Apipe está en 0 y Dpipe existe, calcular Apipe
        if (Apipe <= 0.0 && Dpipe > 0.0) {
            Apipe = Math.PI * Math.pow(Dpipe, 2) / 4.0;
        }

        // área efectiva usada (no puede ser mayor que la tubería)
        final double effectiveAorifice = Math.min(Aorifice, Math.max(1e-12, Apipe));
        final double Cd = ORIFICE_DISCHARGE_COEFF;
        final double finalRho = rho;

        return t -> {
            double dp = deltaP.value(t); // Pa
            if (Double.isNaN(dp) || Double.isInfinite(dp) || dp <= 0.0) return 0.0;

            // ecuación de orificio
            double q = Cd * effectiveAorifice * Math.sqrt((2.0 * dp) / finalRho);
            if (Double.isNaN(q) || Double.isInfinite(q) || q <= 0.0) return 0.0;
            return q;
        };
    }

    /**
     * Pausar cuando el volumen ya superó o alcanzó el nivel objetivo (vOk).
     */
    private boolean shouldPauseFilling(double volume, double vMin, double vOk, double vMax) {
        return volume >= vOk;
    }

    /**
     * Comprueba que el tanque central tenga más del porcentaje mínimo.
     */
    private boolean hasEnoughCentralWater() {
        double centralPercent = (waterTankCentralLink.getCurrentVolume() /
                waterTankCentralLink.getMaximumVolume()) * 100.0;
        return centralPercent > MIN_CENTRAL_LEVEL;
    }

    /**
     * Actualiza volúmenes en DTO. NOTA: ahora el consumo del tanque central se hace antes de llamar a este método
     * (el bucle principal controla cuánto consumir).
     */
    private double updateWaterVolumes(double increment, double vMax) {
        double newVolume = Math.max(0.0,
                Math.min(vMax, houseDto.getWaterTank().getCurrentVolume() + increment));
        houseDto.getWaterTank().setCurrentVolume(newVolume);
        houseDto.getWaterTank().setCurrentPercentage((newVolume / vMax) * 100.0);
        houseDto.getWaterTank().setFilling(!(newVolume >= houseDto.getWaterTank().getSaveVolume()));
        return newVolume;
    }

    /**
     * Notifica al cliente por WebSocket.
     */
    private void notifyClients() {
        messagingTemplate.convertAndSend("/topic/house-water-tank", houseDto);
    }
}
