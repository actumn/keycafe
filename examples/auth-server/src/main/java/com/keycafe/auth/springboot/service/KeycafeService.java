package com.keycafe.auth.springboot.service;

import io.keycafe.client.Keycafe;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration

public class KeycafeService {

    private Keycafe keycafe;

    public KeycafeService(){
        this.keycafe =  new Keycafe();
        keycafe.connect();
    }

    @Bean
    @Scope(value = "singleton")
    public Keycafe getKeycafe() {
        return this.keycafe;
    }
}
