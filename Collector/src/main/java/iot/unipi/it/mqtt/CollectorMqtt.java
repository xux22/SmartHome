package iot.unipi.it.mqtt;

import iot.unipi.it.coap.resources.Actuator;
import iot.unipi.it.database.DBManager;
import iot.unipi.it.database.DBManagerFactory;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;

public class CollectorMqtt implements MqttCallback {

    private String topic = "temperature";
    private String broker = "tcp://127.0.0.1:1883";
    private String clientId = "Collector";
    private MqttClient mqttClient = null;
    private DBManager dbManager = DBManagerFactory.getDbInstance();

    public CollectorMqtt() {
        try {
            mqttClient = new MqttClient(broker, clientId);
            System.out.println("Connecting to broker: " + broker);
            mqttClient.setCallback(this);
            mqttClient.connect();
            mqttClient.subscribe(topic);
        } catch (MqttException me) {
            me.printStackTrace();
        }
    }

    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost cause: " + cause.getCause().getMessage());
    }

    public void publish(String content, String node){
        try{
            MqttMessage message = new MqttMessage(content.getBytes());
            this.mqttClient.publish("actuator_" + node, message);
        }catch(MqttException me){
            me.printStackTrace();
        }
    }

    public void messageArrived(String topic, MqttMessage message) {
        System.out.println(String.format("[%s] %s", topic, new String(message.getPayload())));
        JSONObject responseText = new JSONObject(new String(message.getPayload()));
        String sensorId = responseText.getString("sensor_id");
        if(responseText.has("temperature")){
            int timestamp = responseText.getInt("timestamp");
            double temperature = responseText.getDouble("temperature");
            dbManager.insert(sensorId, temperature, timestamp);
            System.out.println("Obtained new measurement from: " + sensorId + "\n" +
                    "temperature: " + temperature + ",\n" +
                    "timestamp: " + timestamp);
            if(temperature >= 25 || temperature <= 18){
                Actuator a = dbManager.retrieveAct(sensorId);
                if(!a.isActive()){
                    System.out.println("Actuator not available yet!");
                    return;
                }
                String payload = "{\"mode\": on, ";
                if(temperature >= 25){
                    payload += "\"value\": down}";
                }else{
                    payload += "\"value\": up}";
                }
                publish(payload, a.getResourceName());
            }else if(temperature == 21){
                Actuator a = dbManager.retrieveAct(sensorId);
                if(!a.isActive()){
                    System.out.println("Actuator not available yet!");
                    return;
                }
                String payload = "{\"mode\": off}";
                publish(payload, a.getResourceName());
            }
        }else{
            System.out.println("Error: message format from sensor at IP " + sensorId + " is not correct");
        }
    }

    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("Delivery completed");
    }
}
