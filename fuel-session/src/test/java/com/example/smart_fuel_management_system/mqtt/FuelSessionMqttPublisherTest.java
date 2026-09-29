package com.example.smart_fuel_management_system.mqtt;

import com.example.smart_fuel_management_system.config.MqttProperties;
import com.example.smart_fuel_management_system.exception.MqttMessageProcessingException;
import com.example.smart_fuel_management_system.mqtt.dto.FuelSessionMqttErrorResponse;
import com.example.smart_fuel_management_system.mqtt.dto.FuelSessionPauseMqttResponse;
import com.example.smart_fuel_management_system.mqtt.dto.FuelSessionResumeMqttResponse;
import com.example.smart_fuel_management_system.mqtt.dto.FuelSessionStartMqttResponse;
import com.example.smart_fuel_management_system.mqtt.dto.FuelSessionStopMqttResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FuelSessionMqttPublisherTest {

    @Mock
    private MqttConnectionManager connectionManager;

    @Mock
    private MqttProperties properties;

    @Mock
    private MqttProperties.Topics topics;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FuelSessionMqttPublisher publisher;

    @Test
    void startResponseTopic_shouldBuildTopic() {
        // given
        UUID pumpId = UUID.randomUUID();

        when(properties.getTopics()).thenReturn(topics);
        when(topics.getStartResponsePrefix())
                .thenReturn("fuel/start/response");

        // when
        String result = publisher.startResponseTopic(pumpId);

        // then
        assertThat(result)
                .isEqualTo("fuel/start/response/" + pumpId);
    }

    @Test
    void stopResponseTopic_shouldBuildTopic() {
        // given
        UUID pumpId = UUID.randomUUID();

        when(properties.getTopics()).thenReturn(topics);
        when(topics.getStopResponsePrefix())
                .thenReturn("fuel/stop/response");

        // when
        String result = publisher.stopResponseTopic(pumpId);

        // then
        assertThat(result)
                .isEqualTo("fuel/stop/response/" + pumpId);
    }

    @Test
    void pauseResponseTopic_shouldBuildTopic() {
        // given
        UUID pumpId = UUID.randomUUID();

        when(properties.getTopics()).thenReturn(topics);
        when(topics.getPauseResponsePrefix())
                .thenReturn("fuel/pause/response");

        // when
        String result = publisher.pauseResponseTopic(pumpId);

        // then
        assertThat(result)
                .isEqualTo("fuel/pause/response/" + pumpId);
    }

    @Test
    void resumeResponseTopic_shouldBuildTopic() {
        // given
        UUID pumpId = UUID.randomUUID();

        when(properties.getTopics()).thenReturn(topics);
        when(topics.getResumeResponsePrefix())
                .thenReturn("fuel/resume/response");

        // when
        String result = publisher.resumeResponseTopic(pumpId);

        // then
        assertThat(result)
                .isEqualTo("fuel/resume/response/" + pumpId);
    }

    @Test
    void publishStartResponse_shouldSerializeAndPublish()
            throws Exception {

        // given
        UUID pumpId = UUID.randomUUID();
        FuelSessionStartMqttResponse response = null;
        byte[] body = "payload".getBytes();

        when(properties.getTopics())
                .thenReturn(topics);

        when(topics.getStartResponsePrefix())
                .thenReturn("fuel/start/response");

        when(objectMapper.writeValueAsBytes(response))
                .thenReturn(body);

        when(properties.getDefaultQos())
                .thenReturn(1);

        // when
        publisher.publishStartResponse(pumpId, response);

        // then
        verify(objectMapper)
                .writeValueAsBytes(response);

        verify(connectionManager)
                .publish(
                        "fuel/start/response/" + pumpId,
                        body,
                        1
                );
    }

    @Test
    void publishPauseResponse_shouldSerializeAndPublish()
            throws Exception {

        // given
        UUID pumpId = UUID.randomUUID();
        FuelSessionPauseMqttResponse response = null;
        byte[] body = "payload".getBytes();

        when(properties.getTopics())
                .thenReturn(topics);

        when(topics.getPauseResponsePrefix())
                .thenReturn("fuel/pause/response");

        when(objectMapper.writeValueAsBytes(response))
                .thenReturn(body);

        when(properties.getDefaultQos())
                .thenReturn(1);

        // when
        publisher.publishPauseResponse(pumpId, response);

        // then
        verify(connectionManager)
                .publish(
                        "fuel/pause/response/" + pumpId,
                        body,
                        1
                );
    }

    @Test
    void publishResumeResponse_shouldSerializeAndPublish()
            throws Exception {

        // given
        UUID pumpId = UUID.randomUUID();
        FuelSessionResumeMqttResponse response = null;
        byte[] body = "payload".getBytes();

        when(properties.getTopics())
                .thenReturn(topics);

        when(topics.getResumeResponsePrefix())
                .thenReturn("fuel/resume/response");

        when(objectMapper.writeValueAsBytes(response))
                .thenReturn(body);

        when(properties.getDefaultQos())
                .thenReturn(1);

        // when
        publisher.publishResumeResponse(pumpId, response);

        // then
        verify(connectionManager)
                .publish(
                        "fuel/resume/response/" + pumpId,
                        body,
                        1
                );
    }

    @Test
    void publishStopResponse_shouldSerializeAndPublish()
            throws Exception {

        // given
        UUID pumpId = UUID.randomUUID();
        FuelSessionStopMqttResponse response = null;
        byte[] body = "payload".getBytes();

        when(properties.getTopics())
                .thenReturn(topics);

        when(topics.getStopResponsePrefix())
                .thenReturn("fuel/stop/response");

        when(objectMapper.writeValueAsBytes(response))
                .thenReturn(body);

        when(properties.getDefaultQos())
                .thenReturn(1);

        // when
        publisher.publishStopResponse(pumpId, response);

        // then
        verify(connectionManager)
                .publish(
                        "fuel/stop/response/" + pumpId,
                        body,
                        1
                );
    }

    @Test
    void publishStartResponse_shouldThrowMqttMessageProcessingException_whenSerializationFails()
            throws Exception {

        // given
        UUID pumpId = UUID.randomUUID();
        FuelSessionStartMqttResponse response = null;

        when(properties.getTopics())
                .thenReturn(topics);

        when(topics.getStartResponsePrefix())
                .thenReturn("fuel/start/response");

        when(objectMapper.writeValueAsBytes(response))
                .thenThrow(
                        new JsonProcessingException("Serialization failed") {}
                );

        // when & then
        assertThatThrownBy(() ->
                publisher.publishStartResponse(pumpId, response)
        ).isInstanceOf(MqttMessageProcessingException.class);
    }

    @Test
    void publishError_shouldPublishErrorResponse_whenExceptionOccurs()
            throws Exception {

        // given
        UUID pumpId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        byte[] body = "payload".getBytes();

        when(objectMapper.writeValueAsBytes(
                any(FuelSessionMqttErrorResponse.class)
        )).thenReturn(body);

        when(properties.getDefaultQos())
                .thenReturn(1);

        // when
        publisher.publishError(
                "fuel/error",
                "START",
                pumpId,
                sessionId,
                new IllegalArgumentException("Invalid request")
        );

        // then
        verify(connectionManager)
                .publish(
                        eq("fuel/error"),
                        eq(body),
                        eq(1)
                );
    }

    @Test
    void publishError_shouldThrowMqttMessageProcessingException_whenPublishingFails() throws JsonProcessingException {
        // given
        UUID pumpId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        when(objectMapper.writeValueAsBytes(
                any(FuelSessionMqttErrorResponse.class)
        )).thenReturn("payload".getBytes());

        when(properties.getDefaultQos())
                .thenReturn(1);

        org.mockito.Mockito.doThrow(
                new RuntimeException("MQTT failed")
        ).when(connectionManager).publish(
                eq("fuel/error"),
                any(byte[].class),
                eq(1)
        );

        // when & then
        assertThatThrownBy(() ->
                publisher.publishError(
                        "fuel/error",
                        "START",
                        pumpId,
                        sessionId,
                        new IllegalArgumentException("Invalid request")
                )
        ).isInstanceOf(MqttMessageProcessingException.class);
    }

    @Test
    void publishError_shouldHandleEntityNotFoundException() throws Exception {
        // given
        UUID pumpId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        when(objectMapper.writeValueAsBytes(
                any(FuelSessionMqttErrorResponse.class)
        )).thenReturn("payload".getBytes());

        when(properties.getDefaultQos())
                .thenReturn(1);

        // when
        publisher.publishError(
                "fuel/error",
                "STOP",
                pumpId,
                sessionId,
                new EntityNotFoundException("Session not found")
        );

        // then
        verify(connectionManager).publish(
                eq("fuel/error"),
                any(byte[].class),
                eq(1)
        );
    }

    @Test
    void publishError_shouldHandleIllegalStateException() throws Exception {
        // given
        UUID pumpId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        when(objectMapper.writeValueAsBytes(
                any(FuelSessionMqttErrorResponse.class)
        )).thenReturn("payload".getBytes());

        when(properties.getDefaultQos())
                .thenReturn(1);

        // when
        publisher.publishError(
                "fuel/error",
                "PAUSE",
                pumpId,
                sessionId,
                new IllegalStateException("Session is not active")
        );

        // then
        verify(connectionManager).publish(
                eq("fuel/error"),
                any(byte[].class),
                eq(1)
        );
    }
}
