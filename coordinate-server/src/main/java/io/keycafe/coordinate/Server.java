package io.keycafe.coordinate;

import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class Server {
    public static void main(String[] args) {
        CMServerStub serverStub = new CMServerStub();
        serverStub.setAppEventHandler(new ServerEventHandler(serverStub));
        serverStub.startCM();
    }
}
