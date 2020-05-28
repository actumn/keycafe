package io.keycafe.coordinate;

import kr.ac.konkuk.ccslab.cm.event.*;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerEventHandler implements CMAppEventHandler {
    private static final Logger logger = LogManager.getLogger(ServerEventHandler.class);

    private final Map<String, String> discoveryMap = new ConcurrentHashMap<>();

    private final CMServerStub serverStub;

    public ServerEventHandler(CMServerStub serverStub) {
        this.serverStub = serverStub;
    }


    @Override
    public void processEvent(CMEvent cme) {
        switch(cme.getType()) {
            case CMInfo.CM_SESSION_EVENT:
                sessionEvent((CMSessionEvent) cme);
                break;
            case CMInfo.CM_INTEREST_EVENT:
                interestEvent((CMInterestEvent) cme);
                break;
            case CMInfo.CM_USER_EVENT:
                userEvent((CMUserEvent) cme);
                break;
            default:
                logger.info("Unhandled processEvent " + cme.getType() + " " + cme.getID());
                break;
        }
    }

    private void sessionEvent(CMSessionEvent sessionEvent) {
        switch (sessionEvent.getID()) {
            case CMSessionEvent.LOGIN:
                logger.info("["+sessionEvent.getUserName()+"] requests login.");
                break;
            case CMSessionEvent.LOGOUT:
                logger.info("["+sessionEvent.getUserName()+"] logs out.");
                discoveryMap.remove(sessionEvent.getUserName());
                break;
            case CMSessionEvent.JOIN_SESSION:
                logger.info("["+sessionEvent.getUserName()+"] requests to join session("+sessionEvent.getSessionName()+").");
                break;
            case CMSessionEvent.ADD_BLOCK_SOCKET_CHANNEL:
                break;
            default:
                logger.info("Unhandled sessionEvent " + sessionEvent.getType() + " " + sessionEvent.getID());
                break;
        }
    }

    private void interestEvent(CMInterestEvent interestEvent) {
        switch(interestEvent.getID())
        {
            case CMInterestEvent.USER_ENTER:
                logger.info("["+interestEvent.getUserName()+"] enters " +
                        "group("+interestEvent.getCurrentGroup()+") in " +
                        "session("+interestEvent.getHandlerSession()+").");
                break;
            case CMInterestEvent.USER_LEAVE:
                logger.info("["+interestEvent.getUserName()+"] leaves " +
                        "group("+interestEvent.getHandlerGroup()+") in " +
                        "session("+interestEvent.getHandlerSession()+").");
                break;
            default:
                logger.info("Unhandled interestEvent " + interestEvent.getType() + " " + interestEvent.getID());
                break;
        }
    }

    private void userEvent(CMUserEvent userEvent) {
        if ("register-node".equals(userEvent.getStringID())) {
            String nodeId = userEvent.getEventField(CMInfo.CM_STR, "node-id");
            String config = userEvent.getEventField(CMInfo.CM_STR, "node-config");
            logger.info("user event node-id: " + nodeId);
            logger.info("user event node-config" + config);

            for (Map.Entry<String, String> entry : discoveryMap.entrySet()) {
                CMUserEvent sendEvent = new CMUserEvent();
                sendEvent.setStringID("register-node");
                sendEvent.setEventField(CMInfo.CM_STR, "node-id", entry.getKey());
                sendEvent.setEventField(CMInfo.CM_STR, "node-config", entry.getValue());
                serverStub.send(sendEvent, nodeId);
            }
            discoveryMap.put(nodeId, config);
            serverStub.broadcast(userEvent);
            rebalanceSlots();
            return;
        }

        logger.info("unhandled userEvent: " + userEvent.getStringID());
    }

    private static final int CLUSTER_SLOTS = 16384;
    private void rebalanceSlots() {
        logger.info("current server counts: {}", discoveryMap.size());
        int[] splitSlots = Utils.splitNumber(CLUSTER_SLOTS, discoveryMap.size());
        int index = 0;
        for (String nodeId : discoveryMap.keySet()) {
            logger.info("current server: {}, index {}", nodeId, index);

            CMUserEvent sendEvent = new CMUserEvent();
            sendEvent.setStringID("rebalance-slot");
            sendEvent.setEventField(CMInfo.CM_INT, "low", String.valueOf(splitSlots[index]));
            sendEvent.setEventField(CMInfo.CM_INT, "high", String.valueOf(splitSlots[index+1]));
            serverStub.send(sendEvent, nodeId);

            index++;
        }
    }
}
