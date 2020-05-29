package io.keycafe.common;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ClusterCRC16Test {
    @Test
    public void test() {
        assertEquals(0, ClusterCRC16.getSlot(""));
        assertEquals(12739, ClusterCRC16.getSlot("123456789"));
        assertEquals(9308, ClusterCRC16.getSlot("sfger132515"));
        assertEquals(6350, ClusterCRC16.getSlot("hae9Napahngaikeethievubaibogiech"));
        assertEquals(4813, ClusterCRC16.getSlot("AAAAAAAAAAAAAAAAAAAAAA"));
        assertEquals(4054, ClusterCRC16.getSlot("Hello, World!"));
    }
}
