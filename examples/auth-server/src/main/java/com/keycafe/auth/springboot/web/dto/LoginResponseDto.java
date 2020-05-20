package com.keycafe.auth.springboot.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class LoginResponseDto {
    private String token;

    public LoginResponseDto(String token){
        this.token = token;
    }
    /*
    public LoginResponseDto(User entity){
        this.name = entity.getName();
        this.gender = entity.getGender();
        this.email = entity.getEmail();
        this.country = entity.getCountry();
        this.job = entity.getJob();
    }
     */
}
