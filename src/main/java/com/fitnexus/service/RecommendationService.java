package com.fitnexus.service;

import com.fitnexus.dto.RecommendationDto;

import java.util.List;

public interface RecommendationService {
    List<RecommendationDto> getRecommendationsByUserId(String userId);

    RecommendationDto getRecommendationsByActivityId(String activityId);
}
