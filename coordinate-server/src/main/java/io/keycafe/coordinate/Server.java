package io.keycafe.coordinate;

import examples.CMServerEventHandler;
import kr.ac.konkuk.ccslab.cm.manager.CMCommManager;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSUserAccessSimulator;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

import java.util.Scanner;

public class Server {
    public static void main(String[] args) {
        CMServerStub serverStub = new CMServerStub();
        serverStub.setAppEventHandler(new ServerEventHandler(serverStub));
        serverStub.startCM();
    }
}
