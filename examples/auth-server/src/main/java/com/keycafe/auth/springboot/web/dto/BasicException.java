package com.keycafe.auth.springboot.web.dto;

import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@NoArgsConstructor
public class BasicException extends RuntimeException{
    private String error;
    private String message;

    public BasicException(String error, String message){
        this.error = error;
        this.message = message;
    }

}
