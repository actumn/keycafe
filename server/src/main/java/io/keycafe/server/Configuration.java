package io.keycafe.server;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Configuration {
    @JsonProperty("service_port")
    private int servicePort;
    @JsonProperty("cluster_port")
    private int clusterPort;

    public int getServicePort() {
        return servicePort;
    }

    public int getClusterPort() {
        return clusterPort;
    }
}
