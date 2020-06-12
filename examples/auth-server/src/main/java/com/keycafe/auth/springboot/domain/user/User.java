package com.keycafe.auth.springboot.domain.user;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 15, nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String job;

    @Builder
    public User(String name, String email, String password, String gender, String country, String job){
        this.name = name;
        this.email = email;
        this.password = password;
        this.gender = gender;
        this.country = country;
        this.job = job;
    }
}
