package com.keycafe.auth.springboot.web.dto;

import com.keycafe.auth.springboot.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequestDto {
    private String email;
    private String password;

    @Builder
    public LoginRequestDto(String email, String password){
        this.email = email;
        this.password = password;
    }

    public User toEntity(){
        return User.builder()
                .email(email)
                .password(email)
                .build();
    }
}
