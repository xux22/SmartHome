package iot.unipi.it.coap;

import iot.unipi.it.coap.resources.Actuator;
import iot.unipi.it.coap.resources.Thermometer;
import iot.unipi.it.database.DBManager;
import iot.unipi.it.database.DBManagerFactory;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.*;
import org.eclipse.californium.core.coap.Request;
import org.json.JSONObject;

public class CoapObserver {
    private CoapClient client;
    private DBManager dbManager = DBManagerFactory.getDbInstance();
    private Thermometer t;
    private boolean notWorking = false;

    public CoapObserver(Thermometer t){
        client = new CoapClient("coap://[" + t.getNodeAddress() + "]/" + t.getResourceName());
        this.t = t;
    }

    public void startObserving(){
        CoapObserveRelation relation = client.observe(new CoapHandler() {
            public void onLoad(CoapResponse response) {
                JSONObject responseText = new JSONObject(response.getResponseText());
                if(responseText.has("temperature")){
                    int timestamp = responseText.getInt("timestamp");
                    double temperature = responseText.getDouble("temperature");
                    dbManager.insert(t.getNodeAddress(), temperature, timestamp);
                    System.out.println("Obtained new measurement from: " + t.getNodeAddress() + "\n" +
                            "temperature: " + temperature + ",\n" +
                            "timestamp: " + timestamp);
                    if(temperature >= 25 || temperature <= 18){
                        Actuator a = dbManager.retrieveAct(t);
                        if(!a.isActive()){
                            System.out.println("Actuator not available yet!");
                            return;
                        }
                        String payload = "mode=on";
                        if(temperature >= 25){
                            payload += "value=down";
                        }else{
                            payload += "value=up";
                        }
                        Request req = new Request(Code.POST);
                        req.setPayload(payload);
                        req.setURI("coap://[" + a.getNodeAddress() +"]/" + a.getResourceName());
                        req.send();
                    }else if(temperature == 21){
                        Actuator a = dbManager.retrieveAct(t);
                        if(!a.isActive()){
                            System.out.println("Actuator not available yet!");
                            return;
                        }
                        String payload = "mode=off";
                        Request req = new Request(Code.POST);
                        req.setPayload(payload);
                        req.setURI("coap://[" + a.getNodeAddress() +"]/" + a.getResourceName());
                        req.send();
                    }
                }else{
                    System.out.println("Error: message format from sensor at IP " + t.getNodeAddress() + " is not correct");
                }
            }

            public void onError() {
                notWorking = true;
                System.out.println("Nothing to observe at " + t.getNodeAddress() + ", removing it from the DB");
                dbManager.removeTherm(t);
                for(Thermometer tmp : CoapRegistrationHandler.thermometerList){
                    if(tmp.equals(t)){
                        CoapRegistrationHandler.thermometerList.remove(tmp);
                    }
                }
            }
        });
        if(notWorking){
            relation.proactiveCancel();
        }
    }


}
