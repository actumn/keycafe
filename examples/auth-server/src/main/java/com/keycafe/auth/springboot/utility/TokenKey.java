package com.keycafe.auth.springboot.utility;

import io.keycafe.client.util.MurmurHash;

public class TokenKey {
    static final int SEED_MURMURHASH = 0x1234ABCD;

    private final String email;
    private final long issueDate;

    public TokenKey(String email, long issueDate) {
        this.email = email;
        this.issueDate = issueDate;
    }

    public String getKey() {
        String source = email + String.valueOf(issueDate);

        return Long.toString(MurmurHash.hash64A(source.getBytes(), SEED_MURMURHASH), 16);
    }
}