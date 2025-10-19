package com.miumg.wtcenter.config;

import com.miumg.wtcenter.simulink.TankLink;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for creating a TankLink bean with properties
 * loaded from the application configuration.
 */
@Configuration
public class LinksConfig {

    @Value("${water-tank.radio}")
    private double radio;

    @Value("${water-tank.hight}")
    private double hight;

    @Value("${water-tank.min-volume-in-percent}")
    private double minVolumeInPercent;

    @Bean
    public TankLink tankLink() {
        return new TankLink(radio,hight,minVolumeInPercent);
    }
}
