package com.keycafe.auth.springboot.web;

import com.keycafe.auth.springboot.service.AuthService;
import com.keycafe.auth.springboot.service.KeycafeService;
import com.keycafe.auth.springboot.utility.TokenKey;
import com.keycafe.auth.springboot.web.dto.BasicException;
import com.keycafe.auth.springboot.web.dto.LoginRequestDto;
import com.keycafe.auth.springboot.web.dto.LoginResponseDto;
import io.keycafe.client.Keycafe;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class AuthApiController {
    private final AuthService authService;

    @Autowired
    private final KeycafeService keycafeService;
    /*
    @ExceptionHandler(BasicException.class)
    public @ResponseBody BasicException loginError(BasicException ex){
        BasicException basicError = new BasicException();
        basicError.setError("Login Error");
    }
     */

    @GetMapping("/api/v1/login/test")
    public String loginTest(){
        return "hello this is login test!";
    }

    @CrossOrigin(origins="http://localhost:3000")
    @PostMapping("/api/v1/login")
    public ResponseEntity login(@RequestBody LoginRequestDto loginRequestDto){
        System.out.println("40 "+loginRequestDto.getEmail());
        System.out.println("41 "+loginRequestDto.getPassword());

        String email = loginRequestDto.getEmail();
        if(!authService.login(email, loginRequestDto.getPassword()))
            throw new BasicException("Login Error", "로그인 과정에서 에러가 발생했습니다");

        LoginResponseDto response = new LoginResponseDto();

        HttpHeaders responseHeaders = new HttpHeaders();

        TokenKey tokenKey = new TokenKey(email, System.currentTimeMillis());
        Keycafe keycafe = keycafeService.getKeycafe();
        keycafe.set(tokenKey.getKey(), email);

        response.setToken(tokenKey.getKey());

        // responseHeaders.set("Authorization", tokenKey.getKey());
        return new ResponseEntity(response, responseHeaders, HttpStatus.OK);
    }
}
