package com.keycafe.auth.springboot.web.dto;

import com.keycafe.auth.springboot.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserResponseDto {
    private String name;
    private String gender;
    private String email;
    private String country;
    private String job;

    public UserResponseDto(User entity){
        this.name = entity.getName();
        this.gender = entity.getGender();
        this.email = entity.getEmail();
        this.country = entity.getCountry();
        this.job = entity.getJob();
    }
}