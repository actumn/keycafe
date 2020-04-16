package io.keycafe.examples;

import io.keycafe.client.Keycafe;

public class Console {
    public static void main(String[] args) {
        Keycafe keycafe = new Keycafe();
        keycafe.connect();
        System.out.println(keycafe.get("example_key"));
        keycafe.disconnect();
    }
}
