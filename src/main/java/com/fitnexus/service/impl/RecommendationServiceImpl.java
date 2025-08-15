package com.fitnexus.service.impl;

import com.fitnexus.repo.RecommendationRepository;
import com.fitnexus.dto.RecommendationDto;
import com.fitnexus.mapper.RecommendationMapper;
import com.fitnexus.model.Recommendation;
import com.fitnexus.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationRepository recommendationRepository;

    @Override
    public List<RecommendationDto> getRecommendationsByUserId(String userId) {
        List<Recommendation> recommendations = recommendationRepository.findByUserId(userId);
        if (recommendations == null || recommendations.isEmpty()) {
            throw new RuntimeException("Unable to find recommendations for userId: " + userId);
        }
        return recommendations.stream()
                .map(RecommendationMapper::mapToDto)
                .toList();
    }

    @Override
    public RecommendationDto getRecommendationsByActivityId(String activityId) {
        Recommendation recommendation = recommendationRepository.findByActivityId(activityId);
        if (recommendation == null) {
            throw new RuntimeException("Unable to find recommendations for activityId: " + activityId);
        }
        return RecommendationMapper.mapToDto(recommendation);
    }
}
