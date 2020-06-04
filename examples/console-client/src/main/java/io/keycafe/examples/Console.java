package io.keycafe.examples;

import io.keycafe.client.Keycafe;

public class Console {
    public static void main(String[] args) {
        Keycafe keycafe = new Keycafe("localhost", 9814);
        keycafe.connect();
//        System.out.println(keycafe.set("example_key", "KEY"));
//        System.out.println(keycafe.get("example_key"));
//        System.out.println(keycafe.get("no_key"));
        keycafe.getClient().clusterSlots();
        keycafe.disconnect();
    }
}
