package io.keycafe.coordinate;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

public class UtilsTest {
    @Test
    public void test() {
        // Arrange
        int origin = 16384;
        int count = 4;

        // Action
        int[] result = Utils.splitNumber(origin, count);

        // Assert
        assertArrayEquals(new int[]{ 0, 4096, 8192, 12288, 16384 }, result);
    }

    @Test
    public void test2() {
        // Arrange
        int origin = 16384;
        int count = 3;

        // Action
        int[] result = Utils.splitNumber(origin, count);

        // Assert
        assertArrayEquals(new int[]{ 0, 5461, 10922, 16384 }, result);
    }
}
