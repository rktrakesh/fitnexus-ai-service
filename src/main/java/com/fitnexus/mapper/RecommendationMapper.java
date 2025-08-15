package com.fitnexus.mapper;

import com.fitnexus.dto.RecommendationDto;
import com.fitnexus.model.Recommendation;

public class RecommendationMapper {
    public static RecommendationDto mapToDto (Recommendation recommendation) {
        return RecommendationDto.builder()
                .id(recommendation.getId())
                .userId(recommendation.getUserId())
                .activityId(recommendation.getActivityId())
                .activityType(recommendation.getActivityType())
                .recommendations(recommendation.getRecommendations())
                .safety(recommendation.getSafety())
                .improvements(recommendation.getImprovements())
                .suggestions(recommendation.getSuggestions())
                .createdAt(recommendation.getCreatedAt())
                .modifiesAt(recommendation.getModifiesAt())
                .build();
    }
}
