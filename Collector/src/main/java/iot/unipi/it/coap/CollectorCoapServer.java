package iot.unipi.it.coap;

import org.eclipse.californium.core.CoapServer;


public class CollectorCoapServer extends CoapServer {

    public void startServer(){
        this.add(new CoapRegistrationHandler("registration"));
        this.start();
    }
}
