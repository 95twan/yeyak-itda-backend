package com.rodemtree.yeyakitda.mapper;

import com.rodemtree.yeyakitda.dto.request.SignUpRequestDto;
import com.rodemtree.yeyakitda.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "password", source = "encodedPassword")
    UserEntity signUpRequestDtotToUserEntity(SignUpRequestDto dto, String encodedPassword);
}
