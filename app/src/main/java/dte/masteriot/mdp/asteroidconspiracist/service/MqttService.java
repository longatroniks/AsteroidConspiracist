package dte.masteriot.mdp.asteroidconspiracist.service;

import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.MqttClient;
import java.util.concurrent.CompletableFuture;

public class MqttService {
    private Mqtt3AsyncClient client;
    private String serverHost = "192.168.56.1";
    private int serverPort = 1883;

    public void createMQTTclient() {
        client = MqttClient.builder()
                .useMqttVersion3()
                .identifier("asteroid-observation-client")
                .serverHost(serverHost)
                .serverPort(serverPort)
                .automaticReconnectWithDefaultConfig() // Reconnect if connection is lost
                .buildAsync();
    }

    public CompletableFuture<Boolean> connectToBroker(String topic, MessageCallback callback) {
        return client.connectWith().send().thenApply(connAck -> {
            subscribeToTopic(topic, callback); // Subscribe to the topic after connection
            return true;
        }).exceptionally(ex -> {
            Log.e("MqttService", "Failed to connect to broker, retrying...", ex);
            retryConnection(topic, callback);  // Retry the connection
            return false;
        });
    }

    private void retryConnection(String topic, MessageCallback callback) {
        // Wait briefly before retrying
        new android.os.Handler().postDelayed(() -> connectToBroker(topic, callback), 5000);  // Retry after 5 seconds
    }

    public void subscribeToTopic(String topic, MessageCallback callback) {
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

    public void unsubscribeFromTopic(String topic) {
        client.unsubscribeWith()
                .topicFilter(topic)
                .send()
                .whenComplete((unsubAck, throwable) -> {
                    if (throwable != null) {
                        Log.e("MqttService", "Failed to unsubscribe from topic: " + topic, throwable);
                    } else {
                        Log.d("MqttService", "Unsubscribed from topic: " + topic);
                    }
                });
    }

    public void publishObservation(LatLng location, String description) {
        String topic = "AsteroidObservation";
        String message = location.latitude + "," + location.longitude + "," + description;
        client.publishWith().topic(topic).payload(message.getBytes()).send()
                .whenComplete((publish, throwable) -> {
                    if (throwable != null) {
                        Log.e("MqttService", "Failed to publish observation", throwable);
                    } else {
                        Log.d("MqttService", "Published observation to topic: " + topic);
                    }
                });
    }

    public interface MessageCallback {
        void onMessageReceived(String message);
    }
}
