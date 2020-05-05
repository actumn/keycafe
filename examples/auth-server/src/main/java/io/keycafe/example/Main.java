package io.keycafe.example;

public class Main {
    public static void main(String[] args) {
        String email = "test@gmail.com";
        long issueDate = System.currentTimeMillis() / 1000;

        TokenKey key = new TokenKey(email, issueDate);
        System.out.println(key.getKey());
    }
}
