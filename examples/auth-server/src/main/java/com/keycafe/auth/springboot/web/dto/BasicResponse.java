package com.keycafe.auth.springboot.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BasicResponse {
    private String message;
    public BasicResponse(String message){
        this.message = message;
    }
}
