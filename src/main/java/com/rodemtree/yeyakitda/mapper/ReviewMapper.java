package com.rodemtree.yeyakitda.mapper;

import com.rodemtree.yeyakitda.dto.ReviewDto;
import com.rodemtree.yeyakitda.entity.ReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.nickname", target = "userNickname")
    @Mapping(target = "imageUrls", ignore = true) // Todo
    ReviewDto reviewEntityToReviewDto(ReviewEntity entity);
}
