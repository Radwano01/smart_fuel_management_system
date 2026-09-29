package com.example.smart_fuel_management_system.mqtt;

import com.example.smart_fuel_management_system.config.MqttProperties;
import com.example.smart_fuel_management_system.dto.FuelSessionDTO;
import com.example.smart_fuel_management_system.dto.FuelSessionStartResponse;
import com.example.smart_fuel_management_system.dto.StartFuelSessionRequest;
import com.example.smart_fuel_management_system.dto.StopFuelSessionDTO;
import com.example.smart_fuel_management_system.enums.FuelStatusType;
import com.example.smart_fuel_management_system.mqtt.dto.FuelSessionStartMqttRequest;
import com.example.smart_fuel_management_system.mqtt.dto.FuelSessionStartMqttResponse;
import com.example.smart_fuel_management_system.mqtt.dto.FuelSessionStopMqttRequest;
import com.example.smart_fuel_management_system.mqtt.dto.FuelSessionStopMqttResponse;
import com.example.smart_fuel_management_system.service.FuelSessionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuelSessionMqttSubscriberTest {

    @Mock
    private MqttConnectionManager connectionManager;

    @Mock
    private FuelSessionMqttPublisher publisher;

    @Mock
    private FuelSessionService fuelSessionService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MqttProperties properties;

    @Mock
    private MqttProperties.Topics topics;

    @InjectMocks
    private FuelSessionMqttSubscriber subscriber;

    @BeforeEach
    void setUp() {
        when(properties.getTopics()).thenReturn(topics);
        when(properties.getDefaultQos()).thenReturn(1);

        when(topics.getStart()).thenReturn("fuel/start");
        when(topics.getStop()).thenReturn("fuel/stop");
        when(topics.getPause()).thenReturn("fuel/pause");
        when(topics.getResume()).thenReturn("fuel/resume");
    }

    @Test
    void subscribe_shouldRegisterAllListeners() {

        // when
        subscriber.subscribe();

        // then
        verify(connectionManager).registerSubscription(
                eq("fuel/start"),
                eq(1),
                any(IMqttMessageListener.class)
        );

        verify(connectionManager).registerSubscription(
                eq("fuel/stop"),
                eq(1),
                any(IMqttMessageListener.class)
        );

        verify(connectionManager).registerSubscription(
                eq("fuel/pause"),
                eq(1),
                any(IMqttMessageListener.class)
        );

        verify(connectionManager).registerSubscription(
                eq("fuel/resume"),
                eq(1),
                any(IMqttMessageListener.class)
        );
    }

    @Test
    void startListener_shouldStartSessionAndPublishResponse() throws Exception {

        // given
        UUID stationId = UUID.randomUUID();
        UUID pumpId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        MqttMessage message = message("""
                {
                    "stationId": "%s",
                    "pumpId": "%s",
                    "plateNumber": "27ABC123",
                    "rfidTag": "RFID123"
                }
                """.formatted(stationId, pumpId));

        JsonNode payload = new ObjectMapper().readTree(
                new String(message.getPayload())
        );

        FuelSessionStartMqttRequest request =
                new FuelSessionStartMqttRequest(
                        "27ABC123",
                        "RFID123",
                        stationId,
                        pumpId
                );

        FuelSessionStartResponse response =
                new FuelSessionStartResponse(
                        sessionId,
                        FuelStatusType.STARTED,
                        true,
                        null,
                        BigDecimal.valueOf(100)
                );

        when(objectMapper.readTree(any(String.class)))
                .thenReturn(payload);

        when(objectMapper.treeToValue(
                payload,
                FuelSessionStartMqttRequest.class
        )).thenReturn(request);

        when(fuelSessionService.startSession(
                any(StartFuelSessionRequest.class)
        )).thenReturn(response);

        // when
        subscriber.subscribe();

        ArgumentCaptor<IMqttMessageListener> captor =
                ArgumentCaptor.forClass(IMqttMessageListener.class);

        verify(connectionManager, times(4)).registerSubscription(
                any(String.class),
                anyInt(),
                captor.capture()
        );

        IMqttMessageListener startListener =
                captor.getAllValues().get(0);

        startListener.messageArrived(
                "fuel/start",
                message
        );

        // then
        verify(fuelSessionService).startSession(
                any(StartFuelSessionRequest.class)
        );

        verify(publisher).publishStartResponse(
                eq(pumpId),
                any(FuelSessionStartMqttResponse.class)
        );
    }

    @Test
    void stopListener_shouldStopSessionAndPublishResponse() throws Exception {

        // given
        UUID pumpId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        MqttMessage message = message("""
                {
                    "pumpId": "%s",
                    "sessionId": "%s",
                    "liters": 20,
                    "pricePerLiter": 5,
                    "totalCost": 100
                }
                """.formatted(pumpId, sessionId));

        JsonNode payload = new ObjectMapper().readTree(
                new String(message.getPayload())
        );

        FuelSessionStopMqttRequest request =
                new FuelSessionStopMqttRequest(
                        sessionId,
                        pumpId,
                        BigDecimal.valueOf(20),
                        BigDecimal.valueOf(5),
                        BigDecimal.valueOf(100)
                );

        FuelSessionDTO response = FuelSessionDTO.builder()
                .sessionId(sessionId)
                .vehicleId(UUID.randomUUID())
                .stationId(UUID.randomUUID())
                .paymentIntentId("pi_test")
                .liters(BigDecimal.valueOf(20))
                .totalCost(BigDecimal.valueOf(100))
                .status(FuelStatusType.COMPLETED)
                .startedAt(LocalDateTime.now())
                .endedAt(LocalDateTime.now())
                .build();

        when(objectMapper.readTree(any(String.class)))
                .thenReturn(payload);

        when(objectMapper.treeToValue(
                payload,
                FuelSessionStopMqttRequest.class
        )).thenReturn(request);

        when(fuelSessionService.stopSession(
                eq(sessionId),
                any(StopFuelSessionDTO.class)
        )).thenReturn(response);

        // when
        subscriber.subscribe();

        ArgumentCaptor<IMqttMessageListener> captor =
                ArgumentCaptor.forClass(IMqttMessageListener.class);

        verify(connectionManager, times(4)).registerSubscription(
                any(String.class),
                anyInt(),
                captor.capture()
        );

        IMqttMessageListener stopListener =
                captor.getAllValues().get(1);

        stopListener.messageArrived(
                "fuel/stop",
                message
        );

        // then
        verify(fuelSessionService).stopSession(
                eq(sessionId),
                any(StopFuelSessionDTO.class)
        );

        verify(publisher).publishStopResponse(
                eq(pumpId),
                any(FuelSessionStopMqttResponse.class)
        );
    }

    @Test
    void pauseListener_shouldPauseSessionAndPublishResponse() throws Exception {

        // given
        UUID pumpId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        MqttMessage message = message("""
                {
                    "pumpId": "%s",
                    "sessionId": "%s"
                }
                """.formatted(pumpId, sessionId));

        JsonNode payload = new ObjectMapper().readTree(
                new String(message.getPayload())
        );

        when(objectMapper.readTree(any(String.class)))
                .thenReturn(payload);

        // when
        subscriber.subscribe();

        ArgumentCaptor<IMqttMessageListener> captor =
                ArgumentCaptor.forClass(IMqttMessageListener.class);

        verify(connectionManager, times(4)).registerSubscription(
                any(String.class),
                anyInt(),
                captor.capture()
        );

        IMqttMessageListener pauseListener =
                captor.getAllValues().get(2);

        pauseListener.messageArrived(
                "fuel/pause",
                message
        );

        // then
        verify(fuelSessionService).pauseSession(sessionId);

        verify(publisher).publishPauseResponse(
                eq(pumpId),
                any()
        );
    }

    @Test
    void resumeListener_shouldResumeSessionAndPublishResponse() throws Exception {

        // given
        UUID pumpId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        MqttMessage message = message("""
                {
                    "pumpId": "%s",
                    "sessionId": "%s"
                }
                """.formatted(pumpId, sessionId));

        JsonNode payload = new ObjectMapper().readTree(
                new String(message.getPayload())
        );

        when(objectMapper.readTree(any(String.class)))
                .thenReturn(payload);

        // when
        subscriber.subscribe();

        ArgumentCaptor<IMqttMessageListener> captor =
                ArgumentCaptor.forClass(IMqttMessageListener.class);

        verify(connectionManager, times(4)).registerSubscription(
                any(String.class),
                anyInt(),
                captor.capture()
        );

        IMqttMessageListener resumeListener =
                captor.getAllValues().get(3);

        resumeListener.messageArrived(
                "fuel/resume",
                message
        );

        // then
        verify(fuelSessionService).resumeSession(sessionId);

        verify(publisher).publishResumeResponse(
                eq(pumpId),
                any()
        );
    }

    @Test
    void startListener_shouldNotPublishError_whenPumpIdIsMissing() throws Exception {

        // given
        MqttMessage message = message("""
                {
                    "plateNumber": "27ABC123"
                }
                """);

        JsonNode payload = new ObjectMapper().readTree(
                new String(message.getPayload())
        );

        when(objectMapper.readTree(any(String.class)))
                .thenReturn(payload);

        // when
        subscriber.subscribe();

        ArgumentCaptor<IMqttMessageListener> captor =
                ArgumentCaptor.forClass(IMqttMessageListener.class);

        verify(connectionManager, times(4)).registerSubscription(
                any(String.class),
                anyInt(),
                captor.capture()
        );

        IMqttMessageListener startListener =
                captor.getAllValues().get(0);

        startListener.messageArrived(
                "fuel/start",
                message
        );

        // then
        verify(publisher, never()).publishError(
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    private MqttMessage message(String payload) {
        return new MqttMessage(payload.getBytes());
    }
}
