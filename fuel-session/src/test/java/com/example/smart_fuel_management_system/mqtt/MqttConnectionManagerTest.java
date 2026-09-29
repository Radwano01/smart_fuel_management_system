package com.example.smart_fuel_management_system.mqtt;

import com.example.smart_fuel_management_system.config.MqttProperties;
import com.example.smart_fuel_management_system.exception.MqttConnectionException;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class MqttConnectionManagerTest {

    @Mock
    private MqttProperties properties;

    @Mock
    private MqttProperties.Reconnect reconnect;

    @Mock
    private ScheduledExecutorService mqttReconnectExecutor;

    @Mock
    private MqttDefaultFilePersistence persistence;

    @Mock
    private MqttAsyncClient client;

    @Mock
    private MqttToken mqttToken;

    @Mock
    private IMqttDeliveryToken deliveryToken;

    @Mock
    private IMqttMessageListener listener;

    @InjectMocks
    private MqttConnectionManager mqttConnectionManager;

    @Test
    void isConnected_shouldReturnTrue_whenClientIsConnected() {
        // given
        setField(mqttConnectionManager, "client", client);
        when(client.isConnected()).thenReturn(true);

        // when
        boolean result = mqttConnectionManager.isConnected();

        // then
        assertThat(result).isTrue();
    }

    @Test
    void isConnected_shouldReturnFalse_whenClientIsNotConnected() {
        // given
        setField(mqttConnectionManager, "client", client);
        when(client.isConnected()).thenReturn(false);

        // when
        boolean result = mqttConnectionManager.isConnected();

        // then
        assertThat(result).isFalse();
    }

    @Test
    void registerSubscription_shouldSubscribeImmediately_whenClientIsConnected()
            throws Exception {

        // given
        setField(mqttConnectionManager, "client", client);

        when(client.isConnected()).thenReturn(true);

        when(client.subscribe(
                eq("fuel/start"),
                eq(1),
                eq(listener)
        )).thenReturn(mqttToken);

        // when
        mqttConnectionManager.registerSubscription(
                "fuel/start",
                1,
                listener
        );

        // then
        verify(client).subscribe(
                "fuel/start",
                1,
                listener
        );
    }

    @Test
    void registerSubscription_shouldNotSubscribe_whenClientIsDisconnected() throws MqttException {
        // given
        setField(mqttConnectionManager, "client", client);

        when(client.isConnected()).thenReturn(false);

        // when
        mqttConnectionManager.registerSubscription(
                "fuel/start",
                1,
                listener
        );

        // then
        verify(client, never()).subscribe(
                anyString(),
                anyInt(),
                any(IMqttMessageListener.class)
        );
    }

    @Test
    void publish_shouldPublishMessage_whenClientIsConnected()
            throws Exception {

        // given
        byte[] payload = "test-message".getBytes();

        setField(mqttConnectionManager, "client", client);

        when(client.isConnected()).thenReturn(true);

        when(properties.getPublishTimeout())
                .thenReturn(Duration.ofSeconds(5));

        when(client.publish(
                eq("fuel/start"),
                any(org.eclipse.paho.client.mqttv3.MqttMessage.class)
        )).thenReturn(deliveryToken);

        // when
        mqttConnectionManager.publish(
                "fuel/start",
                payload,
                1
        );

        // then
        verify(client).publish(
                eq("fuel/start"),
                any(org.eclipse.paho.client.mqttv3.MqttMessage.class)
        );
    }

    @Test
    void publish_shouldThrowMqttConnectionException_whenPublishFails()
            throws Exception {

        // given
        byte[] payload = "test-message".getBytes();

        setField(
                mqttConnectionManager,
                "client",
                client
        );

        when(client.isConnected())
                .thenReturn(true);

        when(client.publish(
                eq("fuel/start"),
                any(MqttMessage.class)
        )).thenThrow(
                new MqttException(
                        MqttException.REASON_CODE_CLIENT_EXCEPTION
                )
        );

        try (MockedConstruction<MqttAsyncClient> construction =
                     org.mockito.Mockito.mockConstruction(
                             MqttAsyncClient.class,
                             (mock, context) -> {

                                 when(mock.isConnected())
                                         .thenReturn(false);
                             })) {

            // when & then
            assertThatThrownBy(() ->
                    mqttConnectionManager.publish(
                            "fuel/start",
                            payload,
                            1
                    )
            ).isInstanceOf(MqttConnectionException.class);
        }
    }

    @Test
    void connectionLost_shouldScheduleReconnect_whenConnectionIsLost() {
        // given
        when(mqttReconnectExecutor.schedule(
                any(Runnable.class),
                eq(0L),
                eq(TimeUnit.MILLISECONDS)
        )).thenReturn(null);

        // when
        mqttConnectionManager.connectionLost(
                new RuntimeException("Connection lost")
        );

        // then
        verify(mqttReconnectExecutor).schedule(
                any(Runnable.class),
                eq(0L),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    void shutdown_shouldDisconnectAndCloseClient_whenClientIsConnected()
            throws Exception {

        // given
        setField(mqttConnectionManager, "client", client);

        when(client.isConnected()).thenReturn(true);

        when(properties.getConnectionTimeout())
                .thenReturn(Duration.ofSeconds(5));

        when(client.disconnect())
                .thenReturn(mqttToken);

        // when
        mqttConnectionManager.shutdown();

        // then
        verify(client).disconnect();
        verify(client).close();
    }

    @Test
    void shutdown_shouldCloseClient_whenClientIsDisconnected()
            throws Exception {

        // given
        setField(mqttConnectionManager, "client", client);

        when(client.isConnected()).thenReturn(false);

        // when
        mqttConnectionManager.shutdown();

        // then
        verify(client, never()).disconnect();
        verify(client).close();
    }

    @Test
    void initialize_shouldConnectToBroker_whenConnectionSucceeds()
            throws Exception {

        // given
        when(properties.getReconnect())
                .thenReturn(reconnect);

        when(reconnect.getInitialDelay())
                .thenReturn(Duration.ofSeconds(1));

        when(properties.getBrokerUri())
                .thenReturn("tcp://localhost:1883");

        when(properties.getClientId())
                .thenReturn("test-client");

        when(properties.isCleanSession())
                .thenReturn(true);

        when(properties.getConnectionTimeout())
                .thenReturn(Duration.ofSeconds(5));

        when(properties.getKeepAliveInterval())
                .thenReturn(Duration.ofSeconds(30));

        when(properties.getUsername())
                .thenReturn(null);

        when(properties.getPassword())
                .thenReturn(null);

        try (MockedConstruction<MqttAsyncClient> construction =
                     org.mockito.Mockito.mockConstruction(
                             MqttAsyncClient.class,
                             (mock, context) -> {
                                 when(mock.isConnected()).thenReturn(true);
                                 when(mock.connect(any()))
                                         .thenReturn(mqttToken);
                             })) {

            // when
            mqttConnectionManager.initialize();

            // then
            MqttAsyncClient createdClient =
                    construction.constructed().get(0);

            verify(createdClient).connect(any());

            assertThat(mqttConnectionManager.isConnected())
                    .isTrue();
        }
    }

    @Test
    void initialize_shouldScheduleReconnect_whenConnectionFails()
            throws Exception {

        // given
        when(properties.getReconnect())
                .thenReturn(reconnect);

        when(reconnect.getInitialDelay())
                .thenReturn(Duration.ofSeconds(1));

        when(properties.getBrokerUri())
                .thenReturn("tcp://localhost:1883");

        when(properties.getClientId())
                .thenReturn("test-client");

        when(properties.isCleanSession())
                .thenReturn(true);

        when(properties.getConnectionTimeout())
                .thenReturn(Duration.ofSeconds(5));

        when(properties.getKeepAliveInterval())
                .thenReturn(Duration.ofSeconds(30));

        when(properties.getUsername())
                .thenReturn(null);

        when(properties.getPassword())
                .thenReturn(null);

        when(mqttReconnectExecutor.schedule(
                any(Runnable.class),
                anyLong(),
                eq(TimeUnit.MILLISECONDS)
        )).thenReturn(null);

        try (MockedConstruction<MqttAsyncClient> construction =
                     org.mockito.Mockito.mockConstruction(
                             MqttAsyncClient.class,
                             (mock, context) -> {
                                 when(mock.isConnected()).thenReturn(false);
                                 when(mock.connect(any()))
                                         .thenThrow(
                                                 new MqttException(
                                                         MqttException.REASON_CODE_CLIENT_EXCEPTION
                                                 )
                                         );
                             })) {

            // when
            mqttConnectionManager.initialize();

            // then
            verify(mqttReconnectExecutor).schedule(
                    any(Runnable.class),
                    eq(1000L),
                    eq(TimeUnit.MILLISECONDS)
            );

            assertThat(mqttConnectionManager.isConnected())
                    .isFalse();
        }
    }
}
