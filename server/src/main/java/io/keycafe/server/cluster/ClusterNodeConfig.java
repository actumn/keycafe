package io.keycafe.server.cluster;

public class ClusterNodeConfig {
    private final String nodeId;
    private final String hostAddress;
    private final int port;

    public ClusterNodeConfig(String nodeId, String hostAddress, int port) {
        this.nodeId = nodeId;
        this.hostAddress = hostAddress;
        this.port = port;
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
}
