package io.keycafe.server.utils;

import io.keycafe.server.utils.RandomUtils;
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
