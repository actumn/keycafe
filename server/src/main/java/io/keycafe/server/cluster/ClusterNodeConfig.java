package io.keycafe.server.cluster;

public class ClusterNodeConfig {
    private final String nodeId;
    private final String hostAddress;
    private final int port;
    private final int cport;

    public ClusterNodeConfig(String nodeId, String hostAddress, int port, int cport) {
        this.nodeId = nodeId;
        this.hostAddress = hostAddress;
        this.port = port;
        this.cport = cport;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public int getPort() {
        return port;
    }

    public int getCport() {
        return cport;
    }
}
