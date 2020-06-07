package io.keycafe.examples;

import io.keycafe.client.KeycafeCluster;

public class Console {
    public static void main(String[] args) {
//        Keycafe keycafe = new Keycafe("localhost", 9814);
        KeycafeCluster keycafe = new KeycafeCluster("localhost", 9814);
        System.out.println(keycafe.set("example_key", "KEY"));
        System.out.println(keycafe.get("example_key"));
        System.out.println(keycafe.get("no_key"));
        keycafe.close();
    }
}
