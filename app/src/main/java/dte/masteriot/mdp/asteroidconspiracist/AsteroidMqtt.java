package dte.masteriot.mdp.asteroidconspiracist;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.MqttClient;

import dte.masteriot.mdp.asteroidconspiracist.models.Asteroid;

public class AsteroidMqtt
{
    String TAG="TAG_MDPMQTT";
    private String serverHost;
    private int serverPort;
    //Topics for Asteroid:
        //topic=asteroidName/Id
        //topic=asteroidName/Distance
        //topic=asteroidName/Diameter
    //Topics for UFO:
        //topic=UFO/Id/Location

    private String publishingTopic="asteroid"; //Default topic for publishing
    private String subscriptionTopic="asteroid/#"; //Default topic for subscription

    private Mqtt3AsyncClient client;

    private List<Asteroid> asteroidsToPublish;

    AsteroidMqtt()
    {
        serverHost="192.168.56.1";
        serverPort = 1883;
    }

    void createMQTTclient()
    {
        Log.d(TAG, "createMQTTclient()");
        client = MqttClient.builder()
                .useMqttVersion3()
                .identifier("my-mqtt-client-id")
                .serverHost(serverHost)
                .serverPort(serverPort)
                //.useSslWithDefaultConfig()
                .buildAsync();
    }

    //CompletableFuture to manage the asynchronous connection
    CompletableFuture<Boolean> connectToBroker(String messageTopicNewConnection)
    {
        Log.d(TAG, "connectToBroker()");
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        if(client != null) {
            client.connectWith()
                    //.simpleAuth()
                    //.username("")
                    //.password("".getBytes())
                    //.applySimpleAuth()
                    .send()
                    .whenComplete((connAck, throwable) -> {
                        if (throwable != null) {
                            // handle failure
                            Log.d(TAG, "Problem connecting to server:");
                            Log.d(TAG, throwable.toString());
                            future.complete(false); // indicate failure
                        } else {
                            // connected -> setup subscribes and publish a message
                            Log.d(TAG, "Connected to server");
                            future.complete(true); // indicate success
                            //TextViewConnection.setText("Connected to server and subscribed to topic");
                            subscribeToTopic();
                            //publishMessage(this.publishingTopic,"New Connection:Hello Asteroid Conspiracist AG");
                            publishMessage(this.publishingTopic,messageTopicNewConnection);
                        }
                    });
        } else {
            Log.d(TAG, "Cannot connect client (null)");
            //TextViewConnection.setText("No Connected to server");
            future.complete(false); // indicate failure
        }
        return future;
    }

    void subscribeToTopic()
    {
        Log.d(TAG, "subscribeToTopic()");
        client.subscribeWith()
                .topicFilter(subscriptionTopic)
                .callback(publish -> {
                    Log.d(TAG, "Message received");
                    // Process the received message
                    // "publish" is an object of class Mqtt3Publish, which can be used to
                    // obtain the information of the received message
                    String payloadString = new String(publish.getPayloadAsBytes());
                    //TextViewSubscribed.setText("(topic:"+subscriptionTopic+"):"+payloadString);

                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        // Handle failure to subscribe
                        Log.d(TAG, "Problem subscribing to topic:");
                        Log.d(TAG, throwable.toString());

                    } else {
                        // Handle successful subscription, e.g. logging or incrementing a metric
                        Log.d(TAG, "Subscribed to topic");

                    }
                });
    }
    void publishMessage(String publishingTopic, String Message) {
        Log.d(TAG, "publishMessage()");
        client.publishWith()
                .topic(publishingTopic)
                .payload(Message.getBytes())
                .send()
                .whenComplete((publish, throwable) -> {
                    if (throwable != null) {
                        // handle failure to publish
                        Log.d(TAG, "Problem publishing on topic:");
                        Log.d(TAG, throwable.toString());
                        //TextViewPublished.setText("Problem publishing on topic");

                    } else {
                        // handle successful publish, e.g. logging or incrementing a metric
                        Log.d(TAG, "Message published");
                        //TextViewPublished.setText("(topic:"+publishingTopic+"):"+Message);
                        int i=1;
                    }
                });
    }

    void disconnectFromBroker() {
        if (client != null) {
            client.disconnect()
                    .whenComplete ((result, throwable) -> {
                        if (throwable != null) {
                            // handle failure
                            Log.d(TAG, "Problem disconnecting from server:");
                            Log.d(TAG, throwable.toString());
                        } else {
                            Log.d(TAG, "Disconnected from server");
                        }
                    });
        } else {
            Log.d(TAG, "Cannot disconnect client (null)");
        }
    }
    public void PublishAsteroidInfo(List<Asteroid> asteroids)
    {
        Log.d(TAG, "Publishing by Asteroid:");

        String messageTopic=""; //publishingTopic = "id/name/distance/diameter";//topic=name/distance/diameter
        int startIndex,endIndex;
        String asteroidName;
        String publishingTopicAsteroid;
        this.asteroidsToPublish=asteroids;

        for (int i=0; i<asteroids.size(); i++)
        {
            startIndex = asteroids.get(i).getName().indexOf(" ") + 1;
            endIndex = asteroids.get(i).getName().indexOf(" (");
            asteroidName = asteroids.get(i).getName().substring(startIndex, endIndex);
            Log.d(TAG,  "Topic:"+asteroidName);

            publishingTopicAsteroid="asteroid/"+asteroidName+"/Id";
            Log.d(TAG,  "Topic:"+publishingTopicAsteroid);
            messageTopic=asteroids.get(i).getKey().toString();
            publishMessage(publishingTopicAsteroid,messageTopic);
            Log.d(TAG, "messageTopic:"+messageTopic);

            publishingTopicAsteroid="asteroid/"+asteroidName+"/Distance";
            Log.d(TAG,  "Topic:"+publishingTopicAsteroid);
            messageTopic=String.valueOf(asteroids.get(i).getDistance());
            publishMessage(publishingTopicAsteroid,messageTopic);
            Log.d(TAG, "messageTopic:"+messageTopic);

            publishingTopicAsteroid="asteroid/"+asteroidName+"/Diameter";
            Log.d(TAG,  "Topic:"+publishingTopicAsteroid);
            messageTopic=String.valueOf(asteroids.get(i).getDiameter());
            publishMessage(publishingTopicAsteroid,messageTopic);
            Log.d(TAG, "messageTopic:"+messageTopic);
        }

    }


}
