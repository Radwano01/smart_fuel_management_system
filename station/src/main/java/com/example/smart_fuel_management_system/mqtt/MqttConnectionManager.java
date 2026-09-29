package com.example.smart_fuel_management_system.mqtt;

import com.example.smart_fuel_management_system.config.MqttProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttConnectionManager implements MqttCallback {

    private final MqttProperties properties;
    private final ScheduledExecutorService mqttReconnectExecutor;
    private final MqttDefaultFilePersistence persistence;

    private final Map<String, MqttTopicSubscription> subscriptions = new ConcurrentHashMap<>();
    private final Object connectionMonitor = new Object();
    private final AtomicBoolean reconnectScheduled = new AtomicBoolean(false);
    private final AtomicLong retryDelayMillis = new AtomicLong(0);

    private volatile MqttAsyncClient client;

    @PostConstruct
    public void initialize() {
        retryDelayMillis.set(properties.getReconnect().getInitialDelay().toMillis());
        connectAndSubscribe();
    }

    @PreDestroy
    public void shutdown() {
        synchronized (connectionMonitor) {
            closeClientQuietly();
        }
    }

    public void registerSubscription(String topic, int qos, IMqttMessageListener listener) {
        subscriptions.put(topic, new MqttTopicSubscription(topic, qos, listener));

        if (isConnected()) {
            subscribe(topic, qos, listener);
        }
    }

    public boolean isConnected() {
        MqttAsyncClient currentClient = client;
        return currentClient != null && currentClient.isConnected();
    }

    public void publish(String topic, byte[] payload, int qos) throws MqttException {
        synchronized (connectionMonitor) {
            if (!isConnected()) {
                throw new MqttException(MqttException.REASON_CODE_CLIENT_NOT_CONNECTED);
            }

            MqttMessage message = new MqttMessage(payload);
            message.setQos(qos);
            message.setRetained(false);
            client.publish(topic, message)
                    .waitForCompletion(properties.getConnectionTimeout().toMillis());
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT connection lost: {}", cause == null ? "unknown cause" : cause.getMessage());
        scheduleReconnect();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        // Topic-specific listeners handle subscribed messages.
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Station Service does not publish MQTT messages.
    }

    private void connectAndSubscribe() {
        synchronized (connectionMonitor) {
            try {
                closeClientQuietly();
                client = createClient();
                client.setCallback(this);
                client.connect(buildConnectOptions())
                        .waitForCompletion(properties.getConnectionTimeout().toMillis());
                subscribeRegisteredTopics();
                reconnectScheduled.set(false);
                retryDelayMillis.set(properties.getReconnect().getInitialDelay().toMillis());
                log.info("Connected to MQTT broker {}", properties.getBrokerUri());
            } catch (Exception ex) {
                log.warn("Unable to connect to MQTT broker {}: {}", properties.getBrokerUri(), ex.getMessage());
                scheduleReconnect();
            }
        }
    }

    private MqttAsyncClient createClient() throws MqttException {
        return new MqttAsyncClient(properties.getBrokerUri(), resolveClientId(), persistence);
    }

    private String resolveClientId() {
        String configuredClientId = properties.getClientId();
        if (configuredClientId != null && !configuredClientId.isBlank()) {
            return configuredClientId;
        }

        return "station-service-" + UUID.randomUUID();
    }

    private MqttConnectOptions buildConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(properties.isCleanSession());
        options.setAutomaticReconnect(false);
        options.setConnectionTimeout((int) properties.getConnectionTimeout().toSeconds());
        options.setKeepAliveInterval((int) properties.getKeepAliveInterval().toSeconds());

        if (properties.getUsername() != null && !properties.getUsername().isBlank()) {
            options.setUserName(properties.getUsername());
        }

        if (properties.getPassword() != null && !properties.getPassword().isBlank()) {
            options.setPassword(properties.getPassword().toCharArray());
        }

        return options;
    }

    private void subscribeRegisteredTopics() throws MqttException {
        for (MqttTopicSubscription subscription : subscriptions.values()) {
            subscribe(subscription.topic(), subscription.qos(), subscription.listener());
        }
    }

    private void subscribe(String topic, int qos, IMqttMessageListener listener) {
        synchronized (connectionMonitor) {
            if (!isConnected()) {
                return;
            }

            try {
                client.subscribe(topic, qos, listener)
                        .waitForCompletion(properties.getConnectionTimeout().toMillis());
                log.info("Subscribed to MQTT topic {} with QoS {}", topic, qos);
            } catch (Exception ex) {
                log.warn("Unable to subscribe to MQTT topic {}: {}", topic, ex.getMessage());
            }
        }
    }

    private void scheduleReconnect() {
        if (!reconnectScheduled.compareAndSet(false, true)) {
            return;
        }

        long delayMillis = retryDelayMillis.get();
        mqttReconnectExecutor.schedule(() -> {
            reconnectScheduled.set(false);
            connectAndSubscribe();

            if (!isConnected()) {
                long currentDelay = retryDelayMillis.get();
                long nextDelay = Math.min(
                        currentDelay * 2,
                        properties.getReconnect().getMaxDelay().toMillis()
                );
                retryDelayMillis.set(nextDelay);
                scheduleReconnect();
            }
        }, delayMillis, TimeUnit.MILLISECONDS);
    }

    private void closeClientQuietly() {
        MqttAsyncClient currentClient = client;
        client = null;

        if (currentClient == null) {
            return;
        }

        try {
            if (currentClient.isConnected()) {
                currentClient.disconnect()
                        .waitForCompletion(properties.getConnectionTimeout().toMillis());
            }
        } catch (Exception ex) {
            log.debug("MQTT disconnect failed: {}", ex.getMessage());
        }

        try {
            currentClient.close();
        } catch (Exception ex) {
            log.debug("MQTT client close failed: {}", ex.getMessage());
        }
    }

    private record MqttTopicSubscription(String topic, int qos, IMqttMessageListener listener) {
    }
}
