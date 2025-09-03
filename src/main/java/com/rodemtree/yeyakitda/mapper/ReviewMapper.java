package com.rodemtree.yeyakitda.mapper;

import com.rodemtree.yeyakitda.dto.ReviewDto;
import com.rodemtree.yeyakitda.entity.ReviewEntity;
import com.rodemtree.yeyakitda.entity.ReviewImageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(source = "entity.user.id", target = "userId")
    @Mapping(source = "entity.user.nickname", target = "userNickname")
    @Mapping(source = "images", target = "imageUrls")
    ReviewDto reviewEntityToReviewDto(ReviewEntity entity, List<ReviewImageEntity> images);

    default List<String> mapImageUrls(List<ReviewImageEntity> images) {
        return images.stream().map(ReviewImageEntity::getImageUrl).toList();
    }
}
