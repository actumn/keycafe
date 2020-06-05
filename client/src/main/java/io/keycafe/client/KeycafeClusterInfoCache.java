package io.keycafe.client;

import io.keycafe.client.util.StringCodec;
import io.keycafe.common.Protocol;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeycafeClusterInfoCache {
    private final Map<String, Keycafe> nodes = new HashMap<>();
    private final Map<Integer, Keycafe> slots = new HashMap<>();

    public void discoverCluster(Keycafe keycafe) {
        List<Object> clusterSlots = keycafe.clusterSlots();

        for (Object slotInfo : clusterSlots) {
            List<Object> slotInfoList = (List<Object>) slotInfo;


            List<Integer> slotNums = getAssignedSlotArray(
                    ((Long) slotInfoList.get(0)).intValue(),
                    ((Long) slotInfoList.get(1)).intValue());

            List<Object> hostInfos = (List<Object>) slotInfoList.get(2);

            String host = StringCodec.decode((byte[]) hostInfos.get(0));
            int port = ((Long) hostInfos.get(1)).intValue();
            assignSlotsToNode(slotNums, host, port);
        }
    }

    public Keycafe setupNodeIfNotExists(String host, int port) {
        String nodeKey = host + ":" + port;
        Keycafe existingNode = nodes.get(nodeKey);
        if (existingNode != null) return existingNode;

        Keycafe node = new Keycafe(host, port);
        node.connect();
        nodes.put(nodeKey, node);
        return node;
    }

    public void assignSlotsToNode(List<Integer> targetSlots, String host, int port) {
        Keycafe target = setupNodeIfNotExists(host, port);
        for (Integer slot : targetSlots) {
            slots.put(slot, target);
        }
    }

    private List<Integer> getAssignedSlotArray(int low, int high) {
        List<Integer> result = new ArrayList<>();
        for (int slot = 0; slot <= high; slot++) {
            result.add(slot);
        }
        return result;
    }

    public Keycafe getKeycafeNode(int slot) {
        return slots.get(slot);
    }

    public void reset() {
        for (Keycafe keycafe : nodes.values()) {
            keycafe.close();
        }
        nodes.clear();
        slots.clear();
    }
}
