package io.keycafe.coordinate;

public class Utils {
    public static int[] splitNumber(int origin, int count) {
        int[] result = new int[count + 1];
        result[0] = 0;
        result[count] = origin;
        int seed = origin / count;
        for (int i = 1; i < count; i++) {
            result[i] = seed * i;
        }
        return result;
    }
}
