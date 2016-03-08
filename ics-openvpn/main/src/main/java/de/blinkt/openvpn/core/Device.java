package de.blinkt.openvpn.core;

public class Device {
    private String id;
    private String name;
    private Boolean state;

    public Device() {}

    public Device(String id, String name, Boolean state) {
        this.id = id;
        this.name = name;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return name;
    }

}
