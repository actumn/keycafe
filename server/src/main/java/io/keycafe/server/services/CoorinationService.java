package io.keycafe.server.services;

import io.keycafe.server.cluster.CoordinationClientEventHandler;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class CoorinationService implements Service {
    private final CMClientStub clientStub = new CMClientStub();

    @Override
    public void run() throws Exception {
        clientStub.setAppEventHandler(new CoordinationClientEventHandler());
        clientStub.startCM();
    }

    @Override
    public void close() {
        clientStub.terminateCM();
    }
}
