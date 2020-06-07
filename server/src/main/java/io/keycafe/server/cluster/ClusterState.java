package io.keycafe.server.cluster;

import io.keycafe.server.Server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClusterState {
    // SlotToKey slotsToKey
    private final Map<String, ClusterNode> nodeMap = new ConcurrentHashMap<>();
    private final ClusterNode[] slots = new ClusterNode[Server.CLUSTER_SLOTS];

    public ClusterState(ClusterNode myself) {
        this.nodeMap.put(myself.getNodeId(), myself);
    }

    public void putNode(String nodeId, ClusterNode node) {
        nodeMap.put(nodeId, node);
    }

    public Map<String, ClusterNode> getNodeMap() {
        return nodeMap;
    }

    public ClusterNode lookupNode(String nodeId) {
        return nodeMap.get(nodeId);
    }

    public void setSlot(ClusterNode node, int slot) {
        slots[slot] = node;
    }

    public ClusterNode getNodeBySlot(int slot) {
        return slots[slot];
    }
}
