package io.keycafe.coordinate;

import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class ServerEventHandler implements CMAppEventHandler {
    private final CMServerStub serverStub;

    public ServerEventHandler(CMServerStub serverStub) {
        this.serverStub = serverStub;
    }


    @Override
    public void processEvent(CMEvent cme) {
        System.out.println(cme.getType());
    }
}
