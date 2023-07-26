package iot.unipi.it.coap;

import iot.unipi.it.coap.resources.Actuator;
import iot.unipi.it.coap.resources.Thermometer;
import iot.unipi.it.database.DBManager;
import iot.unipi.it.database.DBManagerFactory;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.net.InetAddress;
import java.util.*;

public class CoapRegistrationHandler extends CoapResource {

    private DBManager dbManager = DBManagerFactory.getDbInstance();
    public static List<Thermometer> thermometerList = new ArrayList<Thermometer>();
    public static List<Actuator> actuatorList = new ArrayList<Actuator>();

    public CoapRegistrationHandler(String name){
        super(name);
    }

    public void handlePOST(CoapExchange exchange){
        exchange.accept();

        /*

            Resource discovery process, used to get the name of the resource associated to the corresponding address. In
            order to obtain the information needed, we need to issue a get request to the /.well-know/core resource: the
            information we need will be contained in the body of the resource as showed at the following link.
            https://www.researchgate.net/figure/An-example-of-Constrained-RESTful-Environments-CoRE-direct-resource-discovery-and_fig2_262819123
            We therefore need to get the value between angular brackets (since we have just one resource per sensor, we
            will have one occurrence of that term); this value will be inserted in the DB, associated to the corresponding
            IP address.

         */
        InetAddress sensorAddress = exchange.getSourceAddress();
        CoapClient client = new CoapClient("coap://[" + sensorAddress.getHostAddress() + "]:5683/.well-known/core");
        CoapResponse response = client.get();

        String responseCode = response.getCode().toString();
        if(!responseCode.startsWith("2")){
            System.out.println("Error: " + responseCode);
            return;
        }

        String responseText = response.getResponseText();
        System.out.println(sensorAddress.getHostAddress());
        System.out.println(responseText);
        String resourceName = responseText.substring(responseText.indexOf(",</") + 2, responseText.lastIndexOf(">"));
        System.out.println(responseText.substring(responseText.indexOf(",</") + 2, responseText.lastIndexOf(">")));

        boolean toRegister = true;

        if(resourceName.contains("/temp")){
            Thermometer t = new Thermometer(sensorAddress.getHostAddress(), resourceName);
            for(Thermometer tmp : thermometerList){
                if(t.equals(tmp)){
                    toRegister = false;
                    System.out.println("Resource already registered!");
                    break;
                }
            }
            if(toRegister){
                thermometerList.add(t);
                observe(t);
                dbManager.registerTherm(t);
                System.out.println("Thermometer resource inserted in the database; observation started.");
            }
        }else if(resourceName.contains("/act")){
            Actuator a = new Actuator(sensorAddress.getHostAddress(), resourceName, true);
            for(Actuator tmp : actuatorList){
                if(a.equals(tmp)){
                    toRegister = false;
                    System.out.println("Resource already registered!");
                    break;
                }
            }
            if(toRegister){
                actuatorList.add(a);
                dbManager.registerAct(a);
                System.out.println("Actuator resource inserted in the database.");
            }
        }else{
            System.out.println("Error: wrong format of the CoAP registration request");
        }

    }

    private static void observe(Thermometer t){
        CoapObserver obs = new CoapObserver(t);
        obs.startObserving();
    }

}
