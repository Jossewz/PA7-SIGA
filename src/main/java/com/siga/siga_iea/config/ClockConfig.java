package com.siga.siga_iea.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    public static final ZoneId ZONA_BOGOTA = ZoneId.of("America/Bogota");

    @Bean
    @ConditionalOnMissingBean
    public Clock clock() {
        return Clock.system(ZONA_BOGOTA);
    }
}
