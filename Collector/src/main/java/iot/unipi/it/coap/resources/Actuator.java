package iot.unipi.it.coap.resources;

public class Actuator extends Resource {

    private boolean isActive;

    public Actuator(String nodeAddress, String resourceName, boolean isActive) {
        super(nodeAddress, resourceName);
        this.isActive = isActive;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
