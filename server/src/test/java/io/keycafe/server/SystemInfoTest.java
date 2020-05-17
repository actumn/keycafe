package io.keycafe.server;

import org.junit.Test;

import java.net.InetAddress;

public class SystemInfoTest {

    @Test
    public void test() {
        InetAddress hostAddress = SystemInfo.defaultNonLoopbackIpV4Address();

        System.out.println(hostAddress.getHostAddress());
    }
}
