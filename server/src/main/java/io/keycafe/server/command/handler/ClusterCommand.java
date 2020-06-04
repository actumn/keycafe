package io.keycafe.server.command.handler;

import io.keycafe.server.Server;
import io.keycafe.server.cluster.ClusterNode;
import io.keycafe.server.command.reply.*;

import java.util.Arrays;
import java.util.Map;

public class ClusterCommand implements CommandRunnable {
    private final Map<String, ClusterNode> nodeMap;

    public ClusterCommand(Map<String, ClusterNode> nodeMap) {
        this.nodeMap = nodeMap;
    }

    @Override
    public ReplyMessage run(int argc, byte[][] argv) throws Exception {
        if (Arrays.equals(argv[1], "slots".getBytes())) {
            ArrayMessage clusterMessage = new ArrayMessage();

            for (Map.Entry<String, ClusterNode> entries : nodeMap.entrySet()) {
                ArrayMessage nodeMessage = new ArrayMessage();

                ClusterNode node = entries.getValue();
                int start = -1;
                for (int i = 0; i < Server.CLUSTER_SLOTS; i++) {

                    boolean bit = node.bitmapTestBit(i);
                    if (bit) {
                        if (start == -1) start = i;
                    }
                    if (start != -1 && (!bit || i == Server.CLUSTER_SLOTS - 1)) {
                        if (bit && i == Server.CLUSTER_SLOTS - 1) i += 1;

                        if (start == i-1) {
                            // only one slot
                            nodeMessage.add(new IntegerMessage(start));
                            nodeMessage.add(new IntegerMessage(start));
                        } else {
                            nodeMessage.add(new IntegerMessage(start));
                            nodeMessage.add(new IntegerMessage(i-1));
                        }
                        start = -1;

                        ArrayMessage configMessage = new ArrayMessage();
                        configMessage.add(new BulkStringMessage(node.getHostAddress()));
                        configMessage.add(new IntegerMessage(node.getPort()));
                        configMessage.add(new BulkStringMessage(node.getNodeId()));
                        nodeMessage.add(configMessage);
                    }
                }

                clusterMessage.add(nodeMessage);
            }

            return clusterMessage;
        }

        return ErrorMessage.SyntaxErrorMessage;
    }
}
