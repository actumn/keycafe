package io.keycafe.client.stream;

import java.io.BufferedOutputStream;
import java.io.OutputStream;

public class KeycafeOutputStream extends BufferedOutputStream {
    public KeycafeOutputStream(OutputStream outputStream) {
        super(outputStream);
    }
}
