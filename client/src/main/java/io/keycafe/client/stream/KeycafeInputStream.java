package io.keycafe.client.stream;

import io.keycafe.client.exceptions.KeycafeConnectionException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class KeycafeInputStream extends BufferedInputStream {
    public KeycafeInputStream(InputStream inputStream) {
        super(inputStream);
    }

    public long readLongCRLF() {
        return Long.parseLong(readLine());
    }

    public String readLine() {
        final StringBuilder builder = new StringBuilder();

        try {
            while (true) {
                byte b = (byte) this.read();
                if (b == '\r') {
                    byte c = (byte) this.read();
                    if (c == '\n') {
                        break;
                    }
                    builder.append((char) b);
                    builder.append((char) c);
                } else {
                    builder.append((char) b);
                }
            }
        } catch (IOException e) {
            throw new KeycafeConnectionException(e);
        }

        return builder.toString();
    }
}
