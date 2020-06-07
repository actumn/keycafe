package io.keycafe.client.util;

import io.keycafe.client.exceptions.KeycafeExeception;
import io.keycafe.common.Protocol;

import java.io.UnsupportedEncodingException;

public final class StringCodec {
    private StringCodec() { }

    public static byte[] encode(final String str) {
        try {
            return str.getBytes(Protocol.KEYCAFE_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new KeycafeExeception(e);
        }
    }

    public static String decode(final byte[] bytes) {
        try {
            return new String(bytes, Protocol.KEYCAFE_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new KeycafeExeception(e);
        }
    }
}
