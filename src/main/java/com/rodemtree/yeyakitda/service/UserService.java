package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.UserInfoDto;
import com.rodemtree.yeyakitda.dto.request.SignUpRequestDto;
import com.rodemtree.yeyakitda.entity.UserEntity;
import com.rodemtree.yeyakitda.exception.DuplicateException;
import com.rodemtree.yeyakitda.mapper.UserMapper;
import com.rodemtree.yeyakitda.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public void signUp(SignUpRequestDto dto) {
        validateSignUpRequestDto(dto);

        String encodedPassword = passwordEncoder.encode(dto.password());

        UserEntity userEntity = userMapper.signUpRequestDtotToUserEntity(dto, encodedPassword);
        userEntity.setDefaultRole();

        userRepository.save(userEntity);
    }

    private void validateSignUpRequestDto(SignUpRequestDto dto) {
        List<DuplicateException.Field> duplicatedFields = new ArrayList<>();
        if(userRepository.existsByEmail(dto.email())) duplicatedFields.add(DuplicateException.Field.EMAIL);
        if(userRepository.existsByNickname(dto.nickname())) duplicatedFields.add(DuplicateException.Field.NICKNAME);;
        if(userRepository.existsByPhoneNumber(dto.phoneNumber())) duplicatedFields.add(DuplicateException.Field.PHONE_NUMBER);

        if(!duplicatedFields.isEmpty()) throw new DuplicateException(duplicatedFields);
    }

    public UserInfoDto getUserInfo(String userEmail) {
        UserEntity userEntity = userRepository.findByEmail(userEmail).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        return userMapper.userEntityToUserInfoDto(userEntity);
    }
}
