package de.blinkt.openvpn.core;

public class Network {

    private String id;
    private String name;
    private String address;
    private int port;

    public Network(String id, String name, String address, Integer port) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Integer getPort() {
        return port;
    }

    public String toString() {
        return "Network <id="+id+",name="+name+",address="+address+",port="+port+">";
    }
}
