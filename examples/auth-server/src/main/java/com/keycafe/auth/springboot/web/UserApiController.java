package com.keycafe.auth.springboot.web;

import com.keycafe.auth.springboot.service.KeycafeService;
import com.keycafe.auth.springboot.service.UserService;
import com.keycafe.auth.springboot.web.dto.UserResponseDto;
import io.keycafe.client.Keycafe;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class UserApiController {
    private final UserService userService;

    @Autowired
    private final KeycafeService keycafeService;

    @CrossOrigin(origins="http://localhost:3000")
    @GetMapping("/api/v1/user")
    public UserResponseDto getUserInfo(@RequestHeader(name = "Authorization") String authToken){

        System.out.println(authToken);

        Keycafe keycafe = keycafeService.getKeycafe();
        String email = keycafe.get(authToken);
        System.out.println("23   " + email);

        return userService.getUserInfo(email);
    }
}
