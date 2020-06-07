package io.keycafe.server;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class RandomUtilsTest {

    @Test
    public void test() {
        int length = 40;

        String hex = RandomUtils.getRandomHex(40);

        assertEquals(length, hex.length());
    }
}
