package io.keycafe.server.services;

import com.google.gson.Gson;
import io.keycafe.server.Server;
import io.keycafe.server.cluster.ClusterNode;
import kr.ac.konkuk.ccslab.cm.event.*;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CoordinationServiceHandler implements CMAppEventHandler {
    private static final Logger logger = LogManager.getLogger(CoordinationServiceHandler.class);

    private final Server server;

    public CoordinationServiceHandler(Server server) {
        this.server = server;
    }

    @Override
    public void processEvent(CMEvent cme) {
        switch (cme.getType()) {
        case CMInfo.CM_SESSION_EVENT:
            sessionEvent((CMSessionEvent) cme);
            break;
        case CMInfo.CM_DATA_EVENT:
            dataEvent((CMDataEvent) cme);
            break;
        case CMInfo.CM_USER_EVENT:
            userEvent((CMUserEvent) cme);
            break;
        default:
            logger.info("unhandled processEvent " + cme.getType() + " " + cme.getID());
            break;
        }
    }

    private void sessionEvent(CMSessionEvent sessionEvent) {
        switch (sessionEvent.getID()) {
            case CMSessionEvent.LOGIN:
                logger.info("login " + sessionEvent.getUserName());
                break;
            case CMSessionEvent.LOGIN_ACK:
                if(sessionEvent.isValidUser() == 0)
                    logger.info("This client fails authentication by the default server");
                else if(sessionEvent.isValidUser() == -1)
                    logger.info("This client is already in the login-user list");
                else
                    logger.info("This client successfully logs in to the default server");

                break;
            case CMSessionEvent.SESSION_ADD_USER:
            case CMSessionEvent.ADD_BLOCK_SOCKET_CHANNEL_ACK:
            case CMSessionEvent.ADD_BLOCK_SOCKET_CHANNEL:
            case CMSessionEvent.JOIN_SESSION_ACK:
            case CMSessionEvent.CHANGE_SESSION:
                break;
            case CMSessionEvent.UNEXPECTED_SERVER_DISCONNECTION:
                logger.info("Unexpected disconnection from ["+sessionEvent.getChannelName()
                        +"] with key["+sessionEvent.getChannelNum()+"]!");
                logger.info(sessionEvent.getHostAddress());
                logger.info(sessionEvent.getCurrentAddress());
                logger.info(sessionEvent.getCommArch());
                logger.info(sessionEvent.getCreationTime());
                logger.info(sessionEvent.getReturnCode());
                break;
            default:
                logger.info("unhandled sessionEvent " + sessionEvent.getType() + " " + sessionEvent.getID());
                break;
        }
    }

    private void dataEvent(CMDataEvent dataEvent) {
        switch(dataEvent.getID()) {
            case CMDataEvent.INHABITANT:
            case CMDataEvent.NEW_USER:
                logger.info("["+dataEvent.getUserName()+"] " +  dataEvent.getID() + " enters " +
                        "group("+dataEvent.getHandlerGroup()+") in " +
                        "session("+dataEvent.getHandlerSession()+").");
                break;
            case CMDataEvent.REMOVE_USER:
                logger.info("["+dataEvent.getUserName()+"] leaves " +
                        "group("+dataEvent.getHandlerGroup()+") in " +
                        "session("+dataEvent.getHandlerSession()+").");
                break;
            default:
                logger.info("unhandled dataEvent " + dataEvent.getType() + " " + dataEvent.getID());
                break;
        }
    }

    private void userEvent(CMUserEvent userEvent) {
        if ("register-node".equals(userEvent.getStringID())) {
            String nodeId = userEvent.getEventField(CMInfo.CM_STR, "node-id");
            String config = userEvent.getEventField(CMInfo.CM_STR, "node-config");

            final ClusterNode nodeConfiguration = new Gson().fromJson(config, ClusterNode.class);
            server.connect(nodeConfiguration);
            return;
        }

        logger.info("unhandled userEvent: " + userEvent.getStringID());
    }
}
