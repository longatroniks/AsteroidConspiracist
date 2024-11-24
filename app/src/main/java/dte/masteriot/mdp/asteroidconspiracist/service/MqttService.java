package dte.masteriot.mdp.asteroidconspiracist.service;

import android.util.Log;

import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.MqttClient;

import java.util.concurrent.CompletableFuture;

public class MqttService {
    private Mqtt3AsyncClient client;
    private String serverHost = "localhost";
    private int serverPort = 1883;
    private boolean isConnecting = false;

    public void createMQTTClient(String clientId) {
        client = MqttClient.builder()
                .useMqttVersion3()
                .identifier(clientId)
                .serverHost(serverHost)
                .serverPort(serverPort)
                .automaticReconnectWithDefaultConfig()
                .buildAsync();
    }

    public CompletableFuture<Boolean> connectToBroker(String topic, MessageCallback callback) {
        if (isConnecting || (client != null && client.getState().isConnected())) {
            Log.d("MqttService", "Client is already connected or connecting. Skipping connect attempt.");
            return CompletableFuture.completedFuture(true);
        }

        isConnecting = true;

        return client.connectWith().cleanSession(false).send().thenApply(connAck -> {
            isConnecting = false;

            subscribeToTopic(topic, callback);

            String successMessage = "Client connected successfully to broker!";
            publishMessage(topic, successMessage);
            Log.d("MqttService", "Connection message sent to topic: " + topic);

            return true;
        }).exceptionally(ex -> {
            isConnecting = false;
            Log.e("MqttService", "Failed to connect to broker", ex);
            return false;
        });
    }

    public void subscribeToTopic(String topic, MessageCallback callback) {
        if (client == null || !client.getState().isConnected()) {
            Log.w("MqttService", "Cannot subscribe. Client is not connected.");
            return;
        }

        client.subscribeWith()
                .topicFilter(topic)
                .callback(publish -> {
                    String message = new String(publish.getPayloadAsBytes());
                    callback.onMessageReceived(message);
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        Log.e("MqttService", "Failed to subscribe to topic: " + topic, throwable);
                    } else {
                        Log.d("MqttService", "Subscribed to topic: " + topic);
                    }
                });
    }

    public void publishMessage(String topic, String message) {
        if (client == null || !client.getState().isConnected()) {
            Log.w("MqttService", "Cannot publish. Client is not connected.");
            return;
        }

        client.publishWith().topic(topic).payload(message.getBytes()).retain(true).send()
                .whenComplete((publish, throwable) -> {
                    if (throwable != null) {
                        Log.e("MqttService", "Failed to publish message", throwable);
                    } else {
                        Log.d("MqttService", "Published message to topic: " + topic);
                    }
                });
    }

    public void disconnect() {
        if (client != null) {
            client.disconnect()
                    .whenComplete((unused, throwable) -> {
                        if (throwable != null) {
                            Log.e("MqttService", "Failed to disconnect", throwable);
                        } else {
                            Log.d("MqttService", "Disconnected from broker");
                        }
                    });
        } else {
            Log.w("MqttService", "Client is null. Nothing to disconnect.");
        }
    }


    public interface MessageCallback {
        void onMessageReceived(String message);
    }
}
