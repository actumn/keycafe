package com.keycafe.auth.springboot.service;

import io.keycafe.client.KeycafeCluster;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration

public class KeycafeService {

    private KeycafeCluster keycafe;

    public KeycafeService(){
        this.keycafe =  new KeycafeCluster("localhost", 9814);;
    }

    @Bean
    @Scope(value = "singleton")
    public KeycafeCluster getKeycafe() {
        return this.keycafe;
    }
}
