package io.keycafe.server.services;

import com.google.gson.Gson;
import io.keycafe.server.cluster.ClusterNode;
import kr.ac.konkuk.ccslab.cm.event.CMUserEvent;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CoordinationService implements Service {
    private static final Logger logger = LogManager.getLogger(CoordinationService.class);
    private final CMClientStub clientStub = new CMClientStub();
    private final CoordinationServiceHandler handler;

    public CoordinationService(CoordinationServiceHandler handler) {
        this.handler = handler;
    }

    @Override
    public void run() throws Exception {
        clientStub.setAppEventHandler(handler);
        clientStub.startCM();
    }

    public void registerClusterNode(String nodeId, ClusterNode clusterNode) {
        clientStub.loginCM(clusterNode.getNodeId(), "");

        CMUserEvent userEvent = new CMUserEvent();
        userEvent.setStringID("register-node");
        userEvent.setEventField(CMInfo.CM_STR, "node-id", nodeId);
        userEvent.setEventField(CMInfo.CM_STR, "node-config", new Gson().toJson(clusterNode));

        clientStub.send(userEvent, "SERVER");
    }

    @Override
    public void close() {
        clientStub.terminateCM();
    }
}
