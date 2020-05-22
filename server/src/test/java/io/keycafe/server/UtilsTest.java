package io.keycafe.server;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class UtilsTest {

    @Test
    public void test() {
        int length = 40;

        String hex = Utils.getRandomHex(40);

        assertEquals(length, hex.length());
    }
}
