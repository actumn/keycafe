package io.keycafe.server.utils;

public final class BitmapUtils {
    private BitmapUtils() {}

    public static boolean bitmapTestBit(byte[] bitmap, int slot) {
        // slot 은 0 ~ 16383
        // 8로 나누면 0 ~ 2047
        int pos = slot / 8;
        int bit = slot & 7;
        return (bitmap[pos] & (1 << bit)) != 0;
    }
}
