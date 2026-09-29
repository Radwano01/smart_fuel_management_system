package com.example.smart_fuel_management_system.config;

import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

@Configuration
public class MqttConfiguration {

    @Bean(destroyMethod = "shutdownNow")
    public ScheduledExecutorService mqttReconnectExecutor() {
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, "fuel-session-mqtt-reconnect");
            thread.setDaemon(true);
            return thread;
        };

        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }

    @Bean
    public MqttDefaultFilePersistence mqttPersistence(MqttProperties properties) {
        return new MqttDefaultFilePersistence(properties.getPersistenceDirectory());
    }
}
