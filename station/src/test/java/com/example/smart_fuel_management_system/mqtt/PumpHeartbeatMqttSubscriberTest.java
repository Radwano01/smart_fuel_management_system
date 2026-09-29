package com.example.smart_fuel_management_system.mqtt;

import com.example.smart_fuel_management_system.config.MqttProperties;
import com.example.smart_fuel_management_system.enums.PumpStatusType;
import com.example.smart_fuel_management_system.service.PumpHeartbeatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PumpHeartbeatMqttSubscriberTest {

    private static final String SUBSCRIBE_TOPIC = "pump/heartbeat";
    private static final int DEFAULT_QOS = 1;

    @Mock
    private MqttConnectionManager connectionManager;

    @Mock
    private MqttProperties mqttProperties;

    @Mock
    private MqttProperties.Topics mqttTopics;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PumpHeartbeatService pumpHeartbeatService;

    @InjectMocks
    private PumpHeartbeatMqttSubscriber subscriber;

    private IMqttMessageListener captureListener() {
        when(mqttProperties.getTopics()).thenReturn(mqttTopics);
        when(mqttTopics.heartbeatSubscriptionTopic()).thenReturn(SUBSCRIBE_TOPIC);
        when(mqttProperties.getDefaultQos()).thenReturn(DEFAULT_QOS);

        subscriber.subscribe();

        ArgumentCaptor<IMqttMessageListener> captor =
                ArgumentCaptor.forClass(IMqttMessageListener.class);
        verify(connectionManager).registerSubscription(
                eq(SUBSCRIBE_TOPIC), eq(DEFAULT_QOS), captor.capture());
        return captor.getValue();
    }

    private MqttMessage mqttMessage(String payload) {
        return new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void subscribe_shouldRegisterListenerWithConfiguredTopicAndQos() {
        // given
        when(mqttProperties.getTopics()).thenReturn(mqttTopics);
        when(mqttTopics.heartbeatSubscriptionTopic()).thenReturn(SUBSCRIBE_TOPIC);
        when(mqttProperties.getDefaultQos()).thenReturn(DEFAULT_QOS);

        // when
        subscriber.subscribe();

        // then
        verify(connectionManager).registerSubscription(
                eq(SUBSCRIBE_TOPIC), eq(DEFAULT_QOS), any(IMqttMessageListener.class));
    }

    @Test
    void handleHeartbeat_shouldRecordHeartbeat_whenPayloadIsValid() throws Exception {
        // given
        IMqttMessageListener listener = captureListener();
        UUID pumpId = UUID.randomUUID();
        MqttMessage message = mqttMessage(
                "{\"pumpId\":\"" + pumpId + "\",\"status\":\"FUELING\"}");

        // when
        listener.messageArrived(SUBSCRIBE_TOPIC, message);

        // then
        verify(pumpHeartbeatService).recordHeartbeat(pumpId, PumpStatusType.FUELING);
    }

    @Test
    void handleHeartbeat_shouldPassParsedStatusEnum_whenStatusIsOnline() throws Exception {
        // given
        IMqttMessageListener listener = captureListener();
        UUID pumpId = UUID.randomUUID();
        MqttMessage message = mqttMessage(
                "{\"pumpId\":\"" + pumpId + "\",\"status\":\"ONLINE\"}");

        // when
        listener.messageArrived(SUBSCRIBE_TOPIC, message);

        // then
        verify(pumpHeartbeatService).recordHeartbeat(pumpId, PumpStatusType.ONLINE);
    }

    @Test
    void handleHeartbeat_shouldNotCallService_whenJsonIsMalformed() throws Exception {
        // given
        IMqttMessageListener listener = captureListener();
        MqttMessage message = mqttMessage("not-json");

        // when
        listener.messageArrived(SUBSCRIBE_TOPIC, message);

        // then
        verify(pumpHeartbeatService, never()).recordHeartbeat(any(), any());
    }

    @Test
    void handleHeartbeat_shouldNotCallService_whenStatusIsUnknownEnum() throws Exception {
        // given
        IMqttMessageListener listener = captureListener();
        UUID pumpId = UUID.randomUUID();
        MqttMessage message = mqttMessage(
                "{\"pumpId\":\"" + pumpId + "\",\"status\":\"NOT_A_STATUS\"}");

        // when
        listener.messageArrived(SUBSCRIBE_TOPIC, message);

        // then
        verify(pumpHeartbeatService, never()).recordHeartbeat(any(), any());
    }

    @Test
    void handleHeartbeat_shouldNotCallService_whenPumpIdIsMissing() throws Exception {
        // given
        IMqttMessageListener listener = captureListener();
        MqttMessage message = mqttMessage("{\"status\":\"ONLINE\"}");

        // when
        listener.messageArrived(SUBSCRIBE_TOPIC, message);

        // then
        verify(pumpHeartbeatService, never()).recordHeartbeat(any(), any());
    }

    @Test
    void handleHeartbeat_shouldSwallowException_whenServiceThrows() throws Exception {
        // given
        IMqttMessageListener listener = captureListener();
        UUID pumpId = UUID.randomUUID();
        MqttMessage message = mqttMessage(
                "{\"pumpId\":\"" + pumpId + "\",\"status\":\"ONLINE\"}");

        Mockito.doThrow(new RuntimeException("boom"))
                .when(pumpHeartbeatService)
                .recordHeartbeat(eq(pumpId), eq(PumpStatusType.ONLINE));

        // when / then
        assertThatCode(() -> listener.messageArrived(SUBSCRIBE_TOPIC, message))
                .doesNotThrowAnyException();
    }

    @Test
    void handleHeartbeat_shouldHandleBlankPayload() throws Exception {
        // given
        IMqttMessageListener listener = captureListener();
        MqttMessage message = new MqttMessage(new byte[0]);

        // when / then
        assertThatCode(() -> listener.messageArrived(SUBSCRIBE_TOPIC, message))
                .doesNotThrowAnyException();

        verify(pumpHeartbeatService, never()).recordHeartbeat(any(), any());
    }
}