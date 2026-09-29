package com.example.smart_fuel_management_system.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.UUID;

@Setter
@Getter
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {

    private String brokerUri = "tcp://localhost:1883";
    private String clientId = "fuel-session-service";
    private String username;
    private String password;
    private boolean cleanSession = true;
    private Duration connectionTimeout = Duration.ofSeconds(10);
    private Duration keepAliveInterval = Duration.ofSeconds(30);
    private Duration publishTimeout = Duration.ofSeconds(5);
    private int defaultQos = 1;
    private String persistenceDirectory = "target/mqtt/fuel-session";
    private Reconnect reconnect = new Reconnect();
    private Topics topics = new Topics();

    @Setter
    @Getter
    public static class Reconnect {
        private Duration initialDelay = Duration.ofSeconds(2);
        private Duration maxDelay = Duration.ofSeconds(30);
    }

    @Setter
    @Getter
    public static class Topics {
        private String start = "fuel/session/start";
        private String stop = "fuel/session/stop";
        private String pause = "fuel/session/pause";
        private String resume = "fuel/session/resume";
        private String startResponsePrefix = "fuel/session/start/response";
        private String stopResponsePrefix = "fuel/session/stop/response";
        private String pauseResponsePrefix = "fuel/session/pause/response";
        private String resumeResponsePrefix = "fuel/session/resume/response";

    }
}
