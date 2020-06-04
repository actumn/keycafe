package io.keycafe.client.stream;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class KeycafeInputStream extends BufferedInputStream {
    public KeycafeInputStream(InputStream inputStream) {
        super(inputStream);
    }
}
