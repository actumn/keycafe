package com.keycafe.auth.springboot;


import com.keycafe.auth.springboot.service.KeycafeService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args){
        KeycafeService keycafe = new KeycafeService();
        SpringApplication.run(Application.class, args);
    }
}
