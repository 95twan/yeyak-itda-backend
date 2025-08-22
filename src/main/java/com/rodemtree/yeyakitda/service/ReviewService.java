package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.ReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    public List<ReviewDto> getTop10LatestReviews(Long restaurantId) {
        return null;
    }
}
