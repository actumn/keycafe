package io.keycafe.server.cluster;

import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;

public class CoordinationClientEventHandler implements CMAppEventHandler {
    @Override
    public void processEvent(CMEvent cme) {
        System.out.println(cme.getType());
    }
}
