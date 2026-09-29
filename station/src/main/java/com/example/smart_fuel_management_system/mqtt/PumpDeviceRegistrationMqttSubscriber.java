package com.example.smart_fuel_management_system.mqtt;

import com.example.smart_fuel_management_system.config.MqttProperties;
import com.example.smart_fuel_management_system.mqtt.dto.DeviceRegisterMessage;
import com.example.smart_fuel_management_system.mqtt.dto.DeviceRegisterResponse;
import com.example.smart_fuel_management_system.service.PumpDeviceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PumpDeviceRegistrationMqttSubscriber {

    private final MqttConnectionManager connectionManager;
    private final MqttProperties properties;
    private final ObjectMapper objectMapper;
    private final PumpDeviceService pumpDeviceService;

    @PostConstruct
    public void subscribe() {
        connectionManager.registerSubscription(
                properties.getTopics().registrationSubscriptionTopic(),
                properties.getDefaultQos(),
                (topic, message) -> handleRegistration(message)
        );
    }

    private void handleRegistration(MqttMessage message) {
        try {
            DeviceRegisterMessage registration = objectMapper.readValue(
                    message.getPayload(),
                    DeviceRegisterMessage.class
            );
            String deviceId = registration.deviceId().trim();
            UUID pumpId = pumpDeviceService.registerDevice(deviceId);
            byte[] response = objectMapper.writeValueAsBytes(
                new DeviceRegisterResponse(deviceId, pumpId)
            );
            connectionManager.publish(
                properties.getTopics().registrationResponseTopic(deviceId),
                response,
                properties.getDefaultQos()
            );
            log.info("Published pump assignment for device {}: {}", deviceId, pumpId);
        } catch (Exception ex) {
            log.error(
                    "Failed to process pump device registration payload {}",
                    new String(message.getPayload(), StandardCharsets.UTF_8),
                    ex
            );
        }
    }
}