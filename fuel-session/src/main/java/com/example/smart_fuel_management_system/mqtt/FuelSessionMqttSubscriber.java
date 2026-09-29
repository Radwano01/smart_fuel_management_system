package com.example.smart_fuel_management_system.mqtt;

import com.example.smart_fuel_management_system.config.MqttProperties;
import com.example.smart_fuel_management_system.dto.*;
import com.example.smart_fuel_management_system.enums.FuelStatusType;
import com.example.smart_fuel_management_system.exception.MqttMessageProcessingException;
import com.example.smart_fuel_management_system.mqtt.dto.*;
import com.example.smart_fuel_management_system.service.FuelSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FuelSessionMqttSubscriber {

    private final MqttConnectionManager connectionManager;
    private final FuelSessionMqttPublisher publisher;
    private final FuelSessionService fuelSessionService;
    private final ObjectMapper objectMapper;
    private final MqttProperties properties;

    @PostConstruct
    public void subscribe() {
        connectionManager.registerSubscription(
                properties.getTopics().getStart(),
                properties.getDefaultQos(),
                startListener()
        );

        connectionManager.registerSubscription(
                properties.getTopics().getStop(),
                properties.getDefaultQos(),
                stopListener()
        );

        connectionManager.registerSubscription(
                properties.getTopics().getPause(),
                properties.getDefaultQos(),
                pauseListener()
        );

        connectionManager.registerSubscription(
                properties.getTopics().getResume(),
                properties.getDefaultQos(),
                resumeListener()
        );

    }

    private IMqttMessageListener startListener() {
        return (topic, message) -> {
            handleStartMessage(message);
        };
    }

    private IMqttMessageListener stopListener() {
        return (topic, message) -> handleStopMessage(message);
    }

    private IMqttMessageListener pauseListener() {
        return (topic, message) -> handlePauseMessage(message);
    }

    private IMqttMessageListener resumeListener() {
        return (topic, message) -> handleResumeMessage(message);
    }

    private void handleStartMessage(MqttMessage message) {
        UUID pumpId = null;

        try {
            JsonNode payload = readPayload(message);

            pumpId = requireUuid(payload, "pumpId");
            FuelSessionStartMqttRequest request =
                    objectMapper.treeToValue(payload, FuelSessionStartMqttRequest.class);

            FuelSessionStartResponse response =
                    fuelSessionService.startSession(mapStartRequest(request));

            publisher.publishStartResponse(
                    pumpId,
                    mapStartResponse(response)
            );

        } catch (Exception ex) {
            publishStartError(pumpId, ex);
        }
    }

    private void handleStopMessage(MqttMessage message) {
        UUID pumpId = null;
        UUID sessionId = null;

        try {
            JsonNode payload = readPayload(message);

            pumpId = requireUuid(payload, "pumpId");
            sessionId = requireUuid(payload, "sessionId");

            FuelSessionStopMqttRequest request =
                    objectMapper.treeToValue(payload, FuelSessionStopMqttRequest.class);

            FuelSessionDTO response =
                    fuelSessionService.stopSession(sessionId, mapStopRequest(request));

            publisher.publishStopResponse(
                    pumpId,
                    mapStopResponse(pumpId, response)
            );

        } catch (Exception ex) {
            publishStopError(pumpId, sessionId, ex);
        }
    }

    private void handlePauseMessage(MqttMessage message) {
        UUID pumpId = null;
        UUID sessionId = null;

        try {
            JsonNode payload = readPayload(message);

            pumpId = requireUuid(payload, "pumpId");
            sessionId = requireUuid(payload, "sessionId");

            fuelSessionService.pauseSession(sessionId);

            publisher.publishPauseResponse(
                    pumpId,
                    new FuelSessionPauseMqttResponse(
                            sessionId,
                            pumpId,
                            FuelStatusType.PAUSED
                    )
            );

        } catch (Exception ex) {
            publishPauseError(pumpId, sessionId, ex);
        }
    }

    private void handleResumeMessage(MqttMessage message) {
        UUID pumpId = null;
        UUID sessionId = null;

        try {
            JsonNode payload = readPayload(message);

            pumpId = requireUuid(payload, "pumpId");
            sessionId = requireUuid(payload, "sessionId");

            fuelSessionService.resumeSession(sessionId);

            publisher.publishResumeResponse(
                    pumpId,
                    new FuelSessionResumeMqttResponse(
                            sessionId,
                            pumpId,
                            FuelStatusType.STARTED
                    )
            );

        } catch (Exception ex) {
            publishResumeError(pumpId, sessionId, ex);
        }
    }

    private JsonNode readPayload(MqttMessage message) throws JsonProcessingException {
        String body = new String(message.getPayload(), StandardCharsets.UTF_8);
        return objectMapper.readTree(body);
    }

    private UUID requireUuid(JsonNode payload, String fieldName) {
        String text = payload.path(fieldName).asText(null);

        if (text == null || text.isBlank()) {
            throw new MqttMessageProcessingException("Missing required field: " + fieldName);
        }

        try {
            return UUID.fromString(text);
        } catch (IllegalArgumentException ex) {
            throw new MqttMessageProcessingException("Invalid UUID for field: " + fieldName, ex);
        }
    }

    private StartFuelSessionRequest mapStartRequest(FuelSessionStartMqttRequest request) {
        return new StartFuelSessionRequest(
                request.plateNumber(),
                request.rfidTag(),
                request.pumpId()
        );
    }

    private StopFuelSessionDTO mapStopRequest(FuelSessionStopMqttRequest request) {
        return new StopFuelSessionDTO(
                request.liters()
        );
    }

    private FuelSessionStartMqttResponse mapStartResponse(FuelSessionStartResponse response) {
        return new FuelSessionStartMqttResponse(
                response.sessionId(),
                response.status(),
                response.allowed(),
                response.fuelType(),
                response.authorizedAmount()
        );
    }

    private FuelSessionStopMqttResponse mapStopResponse(UUID pumpId, FuelSessionDTO response) {
        return new FuelSessionStopMqttResponse(
                pumpId,
                response.sessionId(),
                response.vehicleId(),
                response.stationId(),
                response.paymentIntentId(),
                response.fuelType(),
                response.liters(),
                response.totalCost(),
                response.status(),
                response.startedAt(),
                response.endedAt()
        );
    }

    private void publishStartError(UUID pumpId, Throwable throwable) {
        if (pumpId == null) {
            log.error("Unable to publish start-session error because pumpId was missing", throwable);
            return;
        }

        publisher.publishError(
                publisher.startResponseTopic(pumpId),
                "start",
                pumpId,
                null,
                throwable
        );
    }

    private void publishStopError(UUID pumpId, UUID sessionId, Throwable throwable) {
        if (pumpId == null) {
            log.error("Unable to publish stop-session error because pumpId was missing", throwable);
            return;
        }

        publisher.publishError(
                publisher.stopResponseTopic(pumpId),
                "stop",
                pumpId,
                sessionId,
                throwable
        );
    }

    private void publishPauseError(UUID pumpId, UUID sessionId, Throwable throwable) {
        if (pumpId == null) {
            log.error("Unable to publish pause-session error because pumpId was missing", throwable);
            return;
        }

        publisher.publishError(
                publisher.pauseResponseTopic(pumpId),
                "pause",
                pumpId,
                sessionId,
                throwable
        );
    }

    private void publishResumeError(UUID pumpId, UUID sessionId, Throwable throwable) {
        if (pumpId == null) {
            log.error("Unable to publish resume-session error because pumpId was missing", throwable);
            return;
        }

        publisher.publishError(
                publisher.resumeResponseTopic(pumpId),
                "resume",
                pumpId,
                sessionId,
                throwable
        );
    }
}