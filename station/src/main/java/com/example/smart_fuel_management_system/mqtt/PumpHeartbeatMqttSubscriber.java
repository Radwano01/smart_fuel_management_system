package com.example.smart_fuel_management_system.mqtt;

import com.example.smart_fuel_management_system.config.MqttProperties;
import com.example.smart_fuel_management_system.mqtt.dto.PumpHeartbeatMqttRequest;
import com.example.smart_fuel_management_system.service.PumpHeartbeatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class PumpHeartbeatMqttSubscriber {

    private final MqttConnectionManager connectionManager;
    private final MqttProperties properties;
    private final ObjectMapper objectMapper;
    private final PumpHeartbeatService pumpHeartbeatService;

    @PostConstruct
    public void subscribe() {
        connectionManager.registerSubscription(
                properties.getTopics().heartbeatSubscriptionTopic(),
                properties.getDefaultQos(),
                (topic, message) -> handleHeartbeat(message)
        );
    }

    private void handleHeartbeat(MqttMessage message) {
        try {
            PumpHeartbeatMqttRequest heartbeat = objectMapper.readValue(
                    message.getPayload(),
                    PumpHeartbeatMqttRequest.class
            );

            if (heartbeat.pumpId() == null) {
                log.warn("Ignoring heartbeat with missing pumpId: {}",
                        new String(message.getPayload(), StandardCharsets.UTF_8));
                return;
            }

            pumpHeartbeatService.recordHeartbeat(heartbeat.pumpId(), heartbeat.status());
        } catch (Exception ex) {
            log.error("Failed to process pump heartbeat payload {}", new String(message.getPayload(), StandardCharsets.UTF_8), ex);
        }
    }
}