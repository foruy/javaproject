package de.blinkt.openvpn.core;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;

public class NetworkConfig {
    private Map<String, String> config = new HashMap<String, String>();
    private List<Network> networks = new ArrayList<Network>();

    public NetworkConfig() {}

    public NetworkConfig(String ca, String cert, String key) {
        config.put("ca", ca);
        config.put("cert", cert);
        config.put("key", key);
    }

    public String getCa() {
        return config.get("ca");
    }

    public void setCa(String ca) {
        config.put("ca", ca);
    }

    public String getCert() {
        return config.get("cert");
    }

    public void setCert(String cert) {
        config.put("cert", cert);
    }

    public String getKey() {
        return config.get("key");
    }

    public void setKey(String key) {
        config.put("key", key);
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public List<Network> getNetworks() {
        return networks;
    }

    public void setNetworks(List<Network> networks) {
        this.networks = networks;
    }

    public void putNetworks(Network network) {
        networks.add(network);
    }

    public Set<String> getNetworksKey() {
        Set<String> ids = new HashSet<String>();
        for (Network net : networks) {
            ids.add(net.getId());
        }
        return ids;
    }
}
