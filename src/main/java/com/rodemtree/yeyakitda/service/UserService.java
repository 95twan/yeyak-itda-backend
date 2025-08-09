package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.request.SignUpRequestDto;
import com.rodemtree.yeyakitda.entity.UserEntity;
import com.rodemtree.yeyakitda.mapper.UserMapper;
import com.rodemtree.yeyakitda.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signUp(SignUpRequestDto dto) {
        userRepository.findByEmail(dto.email());

        UserMapper userMapper = UserMapper.INSTANCE;

        String encodedPassword = passwordEncoder.encode(dto.password());

        UserEntity userEntity = userMapper.signUpRequestDtotToUserEntity(dto, encodedPassword);

        userRepository.save(userEntity);
    }
}
