package io.keycafe.common;

import java.io.UnsupportedEncodingException;

public class Protocol {
    public static final int DEFAULT_PORT = 9814;
    public static final int DEFAULT_CLUSTER_PORT = 19814;
    public static final String KEYCAFE_CHARSET = "UTF-8";

    public enum Command {
        SET, GET, DELETE, CLUSTER;

        private final byte[] raw;

        Command() {
            try {
                raw = this.name().getBytes(KEYCAFE_CHARSET);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        public byte[] getRaw() {
            return raw;
        }
    }
}
