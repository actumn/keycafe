package io.keycafe.server.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public final class RandomUtils {
    private RandomUtils() {}
    private static final byte[] charset = new byte[] {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'
    };

    public static String getRandomHex(int len) {
        StringBuilder builder = new StringBuilder();

        byte[] bytes = getRandomBytes(len);
        for (byte b : bytes) {
            builder.append((char) charset[b & 0x0F]);
        }
        return builder.toString();
    }

    public static byte[] getRandomBytes(int len) {
        try {
            byte[] result = new byte[len];
            int currIndex = 0;
            do {
                byte[] seed = new byte[20];
                new Random().nextBytes(seed);

                MessageDigest md = MessageDigest.getInstance("SHA-1");
                md.update(seed);
                for (byte b : md.digest()) {
                    result[currIndex] = b;
                    currIndex += 1;
                    if (currIndex >= len) break;
                }
            } while (currIndex < len);

            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
