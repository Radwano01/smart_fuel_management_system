package com.example.smart_fuel_management_system.mqtt;

import com.example.smart_fuel_management_system.config.MqttProperties;
import com.example.smart_fuel_management_system.exception.MqttMessageProcessingException;
import com.example.smart_fuel_management_system.mqtt.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FuelSessionMqttPublisher {

    private final MqttConnectionManager connectionManager;
    private final MqttProperties properties;
    private final ObjectMapper objectMapper;

    public void publishStartResponse(UUID pumpId, FuelSessionStartMqttResponse response) {
        publish(startResponseTopic(pumpId), response);
    }

    public void publishPauseResponse(UUID pumpId, FuelSessionPauseMqttResponse response) {
        publish(pauseResponseTopic(pumpId), response);
    }

    public void publishResumeResponse(UUID pumpId, FuelSessionResumeMqttResponse response) {
        publish(resumeResponseTopic(pumpId), response);
    }

    public void publishStopResponse(UUID pumpId, FuelSessionStopMqttResponse response) {
        publish(stopResponseTopic(pumpId), response);
    }

    public void publishError(String responseTopic, String action, UUID pumpId, UUID sessionId, Throwable throwable) {
        FuelSessionMqttErrorResponse response = new FuelSessionMqttErrorResponse(
                action,
                pumpId,
                sessionId,
                classifyError(throwable),
                throwable.getMessage(),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        publish(responseTopic, response);
    }

    public String startResponseTopic(UUID pumpId) {
        return properties.getTopics().getStartResponsePrefix() + "/" + pumpId;
    }

    public String stopResponseTopic(UUID pumpId) {
        return properties.getTopics().getStopResponsePrefix() + "/" + pumpId;
    }

    public String pauseResponseTopic(UUID pumpId) {
        return properties.getTopics().getPauseResponsePrefix() + "/" + pumpId;
    }

    public String resumeResponseTopic(UUID pumpId) {
        return properties.getTopics().getResumeResponsePrefix() + "/" + pumpId;
    }

    private void publish(String topic, Object payload) {
        try {
            byte[] body = objectMapper.writeValueAsBytes(payload);
            connectionManager.publish(topic, body, properties.getDefaultQos());
            log.info("Published MQTT message on {}", topic);
        } catch (Exception ex) {
            throw new MqttMessageProcessingException("Unable to serialize or publish MQTT payload for topic " + topic, ex);
        }
    }

    private String classifyError(Throwable throwable) {
        String simpleName = throwable.getClass().getSimpleName();
        if (throwable instanceof IllegalArgumentException || throwable instanceof com.fasterxml.jackson.core.JsonProcessingException) {
            return "BAD_REQUEST";
        }
        if (throwable instanceof jakarta.persistence.EntityNotFoundException) {
            return "NOT_FOUND";
        }
        if (throwable instanceof IllegalStateException) {
            return "BUSINESS_RULE_VIOLATION";
        }
        if (throwable instanceof MqttMessageProcessingException) {
            return "MQTT_PROCESSING_ERROR";
        }
        return simpleName.toUpperCase();
    }
}
