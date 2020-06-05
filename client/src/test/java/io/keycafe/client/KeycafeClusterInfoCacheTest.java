package io.keycafe.client;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KeycafeClusterInfoCacheTest {
    @Test
    public void test() {
        Keycafe keycafe = new Keycafe("localhost", 9814);
        keycafe.connect();

        KeycafeClusterInfoCache cache = new KeycafeClusterInfoCache();
        cache.discoverCluster(keycafe);
//        assertEquals(1, 1);
    }
}
