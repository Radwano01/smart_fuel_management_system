package com.example.smart_fuel_management_system.mqtt;

import com.example.smart_fuel_management_system.config.MqttProperties;
import com.example.smart_fuel_management_system.mqtt.dto.DeviceRegisterResponse;
import com.example.smart_fuel_management_system.service.PumpDeviceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PumpDeviceRegistrationMqttSubscriberTest {

    private static final String SUBSCRIBE_TOPIC = "pump/devices/register";
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
    private PumpDeviceService pumpDeviceService;

    @InjectMocks
    private PumpDeviceRegistrationMqttSubscriber subscriber;

    private UUID pumpId;

    @BeforeEach
    void setUp() {
        pumpId = UUID.randomUUID();
    }

    private IMqttMessageListener captureListener() {
        when(mqttProperties.getTopics()).thenReturn(mqttTopics);
        when(mqttTopics.registrationSubscriptionTopic()).thenReturn(SUBSCRIBE_TOPIC);
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
        when(mqttTopics.registrationSubscriptionTopic()).thenReturn(SUBSCRIBE_TOPIC);
        when(mqttProperties.getDefaultQos()).thenReturn(DEFAULT_QOS);

        // when
        subscriber.subscribe();

        // then
        verify(connectionManager).registerSubscription(
                eq(SUBSCRIBE_TOPIC), eq(DEFAULT_QOS), any(IMqttMessageListener.class));
    }

    @Test
    void handleRegistration_shouldRegisterDevice_whenPayloadIsValid() throws Exception {
        // given
        IMqttMessageListener listener = captureListener();
        when(pumpDeviceService.registerDevice("DEVICE-001")).thenReturn(pumpId);
        when(mqttTopics.registrationResponseTopic("DEVICE-001"))
                .thenReturn("pump/devices/DEVICE-001/response");
        when(mqttProperties.getDefaultQos()).thenReturn(DEFAULT_QOS);

        MqttMessage message = mqttMessage("{\"deviceId\":\"DEVICE-001\"}");

        // when
        listener.messageArrived(SUBSCRIBE_TOPIC, message);

        // then
        verify(pumpDeviceService).registerDevice("DEVICE-001");
    }

    @Test
    void handleRegistration_shouldPublishResponseToDeviceSpecificTopic_whenRegisterSucceeds() throws Exception {
        // given
        IMqttMessageListener listener = captureListener();
        when(pumpDeviceService.registerDevice("DEVICE-001")).thenReturn(pumpId);
        when(mqttTopics.registrationResponseTopic("DEVICE-001"))
                .thenReturn("pump/devices/DEVICE-001/response");
        when(mqttProperties.getDefaultQos()).thenReturn(DEFAULT_QOS);

        MqttMessage message = mqttMessage("{\"deviceId\":\"DEVICE-001\"}");

        // when
        listener.messageArrived(SUBSCRIBE_TOPIC, message);

        // then
        ArgumentCaptor<byte[]> payloadCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(connectionManager).publish(
                eq("pump/devices/DEVICE-001/response"),
                payloadCaptor.capture(),
                eq(DEFAULT_QOS));

        DeviceRegisterResponse response = objectMapper.readValue(
                payloadCaptor.getValue(), DeviceRegisterResponse.class);
        assertThat(response.deviceId()).isEqualTo("DEVICE-001");
        assertThat(response.pumpId()).isEqualTo(pumpId);
    }

    @Test
    void handleRegistration_shouldTrimDeviceId_beforeRegistering() throws Exception {
        // given
        IMqttMessageListener listener = captureListener();
        when(pumpDeviceService.registerDevice("DEVICE-001")).thenReturn(pumpId);
        when(mqttTopics.registrationResponseTopic("DEVICE-001"))
                .thenReturn("pump/devices/DEVICE-001/response");
        when(mqttProperties.getDefaultQos()).thenReturn(DEFAULT_QOS);

        MqttMessage message = mqttMessage("{\"deviceId\":\"  DEVICE-001  \"}");

        // when
        listener.messageArrived(SUBSCRIBE_TOPIC, message);

        // then
        verify(pumpDeviceService).registerDevice("DEVICE-001");
    }

    @Test
    void handleRegistration_shouldPublishNullPumpId_whenDeviceIsUnassigned() throws Exception {
        // given
        IMqttMessageListener listener = captureListener();
        when(pumpDeviceService.registerDevice("DEVICE-001")).thenReturn(null);
        when(mqttTopics.registrationResponseTopic("DEVICE-001"))
                .thenReturn("pump/devices/DEVICE-001/response");
        when(mqttProperties.getDefaultQos()).thenReturn(DEFAULT_QOS);

        MqttMessage message = mqttMessage("{\"deviceId\":\"DEVICE-001\"}");

        // when
        listener.messageArrived(SUBSCRIBE_TOPIC, message);

        // then
        ArgumentCaptor<byte[]> payloadCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(connectionManager).publish(
                anyString(), payloadCaptor.capture(), anyInt());

        DeviceRegisterResponse response = objectMapper.readValue(
                payloadCaptor.getValue(), DeviceRegisterResponse.class);
        assertThat(response.deviceId()).isEqualTo("DEVICE-001");
        assertThat(response.pumpId()).isNull();
    }

    @Test
    void handleRegistration_shouldNotPublish_whenJsonIsMalformed() throws Exception {
        // given
        IMqttMessageListener listener = captureListener();
        MqttMessage message = mqttMessage("not-json");

        // when
        listener.messageArrived(SUBSCRIBE_TOPIC, message);

        // then
        verify(connectionManager, never()).publish(anyString(), any(byte[].class), anyInt());
        verify(pumpDeviceService, never()).registerDevice(anyString());
    }

    @Test
    void handleRegistration_shouldNotPublish_whenServiceThrows() throws Exception {
        // given
        IMqttMessageListener listener = captureListener();
        when(pumpDeviceService.registerDevice("DEVICE-001"))
                .thenThrow(new RuntimeException("boom"));

        MqttMessage message = mqttMessage("{\"deviceId\":\"DEVICE-001\"}");

        // when
        listener.messageArrived(SUBSCRIBE_TOPIC, message);

        // then
        verify(connectionManager, never()).publish(anyString(), any(byte[].class), anyInt());
    }

    @Test
    void handleRegistration_shouldSwallowException_whenServiceThrows() throws Exception {
        // given
        IMqttMessageListener listener = captureListener();
        when(pumpDeviceService.registerDevice("DEVICE-001"))
                .thenThrow(new RuntimeException("boom"));

        MqttMessage message = mqttMessage("{\"deviceId\":\"DEVICE-001\"}");

        // when / then — should not propagate; message is logged and dropped
        listener.messageArrived(SUBSCRIBE_TOPIC, message);
    }

    @Test
    void handleRegistration_shouldSwallowException_whenPublishFails() throws Exception {
        // given
        IMqttMessageListener listener = captureListener();
        when(pumpDeviceService.registerDevice("DEVICE-001")).thenReturn(pumpId);
        when(mqttTopics.registrationResponseTopic("DEVICE-001"))
                .thenReturn("pump/devices/DEVICE-001/response");
        when(mqttProperties.getDefaultQos()).thenReturn(DEFAULT_QOS);

        doThrow(new MqttException(MqttException.REASON_CODE_CLIENT_NOT_CONNECTED))
                .when(connectionManager)
                .publish(anyString(), any(byte[].class), anyInt());

        MqttMessage message = mqttMessage("{\"deviceId\":\"DEVICE-001\"}");

        // when / then — should not propagate; message is logged and dropped
        listener.messageArrived(SUBSCRIBE_TOPIC, message);

        verify(pumpDeviceService).registerDevice("DEVICE-001");
        verify(connectionManager).publish(anyString(), any(byte[].class), anyInt());
    }

    @Test
    void handleRegistration_shouldHandleBlankPayload() throws Exception {
        // given
        IMqttMessageListener listener = captureListener();
        MqttMessage message = new MqttMessage(new byte[0]);

        // when / then — should not throw, should not call service
        listener.messageArrived(SUBSCRIBE_TOPIC, message);

        verify(pumpDeviceService, never()).registerDevice(anyString());
        verify(connectionManager, never()).publish(anyString(), any(byte[].class), anyInt());
    }
}