package iot.unipi.it;

import iot.unipi.it.coap.CollectorCoapServer;
import iot.unipi.it.database.DBManager;
import iot.unipi.it.database.DBManagerFactory;
import iot.unipi.it.mqtt.CollectorMqtt;

public class Main {

    private static String URL = "jdbc:mysql://localhost/project";
    private static DBManager db = null;
    private static String DB_user = "root";
    private static String DB_password = "root";

    /*
        We create an ad-hoc thread to run the CoAP server in parallel to the MQTT server.
     */
    public static void runCoapServer(){
        new Thread() {
            public void run(){
                CollectorCoapServer coll = new CollectorCoapServer();
                coll.startServer();
            }
        }.start();
    }

    public static void runMqtt() {
        new CollectorMqtt();
    }

    public static void main(String[] args){
        db = DBManagerFactory.createDBManager(URL, DB_user, DB_password);
        runCoapServer();
        runMqtt();
    }
}
