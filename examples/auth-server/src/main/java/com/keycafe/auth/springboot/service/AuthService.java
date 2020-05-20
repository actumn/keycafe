package com.keycafe.auth.springboot.service;

import com.keycafe.auth.springboot.domain.user.User;
import com.keycafe.auth.springboot.domain.user.UserRepository;
import com.keycafe.auth.springboot.web.dto.BasicException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;

    @Transactional
    public Boolean login(String email, String password){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BasicException("Login Error", "해당 사용자가 없습니다"));
        if(!user.getPassword().equals(password))
            throw new BasicException("Login Error", "사용자 정보가 일치하지 않습니다");

        return true;
    }
}
