package com.keycafe.auth.springboot.service;

import com.keycafe.auth.springboot.domain.user.User;
import com.keycafe.auth.springboot.domain.user.UserRepository;
import com.keycafe.auth.springboot.web.dto.BasicException;
import com.keycafe.auth.springboot.web.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public UserResponseDto getUserInfo(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BasicException("Not Found", "해당 사용자가 없습니다"));
        return new UserResponseDto(user);
    }

}
