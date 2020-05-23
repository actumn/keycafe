package io.keycafe.server.cluster;

import io.keycafe.server.Server;

import java.util.Arrays;

public class ClusterNode {
    private final String nodeId;
    private final String hostAddress;
    private final int port;
    private ClusterLink link;
    private byte[] myslots = new byte[Server.CLUSTER_SLOTS / 8];

    public ClusterNode(String nodeId, String hostAddress, int port) {
        this.nodeId = nodeId;
        this.hostAddress = hostAddress;
        this.port = port;

        Arrays.fill(this.myslots, (byte) 0);
    }

    public void link(ClusterLink link) {
        this.link = link;
    }

    public ClusterLink getLink() {
        return link;
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

    public void bitmapSetBit(int slot) {

    }
}
