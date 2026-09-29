package com.example.smart_fuel_management_system.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Setter
@Getter
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {

    private String brokerUri = "tcp://localhost:1883";
    private String clientId = "station-session-service";
    private String username;
    private String password;
    private boolean cleanSession;
    private Duration connectionTimeout;
    private Duration keepAliveInterval;
    private int defaultQos;
    private String persistenceDirectory = "target/mqtt/station";
    private Reconnect reconnect = new Reconnect();
    private Topics topics = new Topics();

    @Setter
    @Getter
    public static class Reconnect {
        private Duration initialDelay;
        private Duration maxDelay;
    }

    @Setter
    @Getter
    public static class Topics {
        private String registration;
        private String registrationResponse;

        public String heartbeatSubscriptionTopic() {
            return "/+/heartbeat";
        }

        public String registrationSubscriptionTopic() {
            return registration;
        }

        public String registrationResponseTopic(String deviceId) {
            return registrationResponse + "/" + deviceId;
        }
    }
}