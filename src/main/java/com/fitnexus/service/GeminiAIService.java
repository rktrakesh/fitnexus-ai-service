package com.fitnexus.service;

import com.fitnexus.dto.RecommendationDto;
import com.fitnexus.model.Activity;
import com.fitnexus.model.Recommendation;

public interface GeminiAIService {

    Recommendation generateRecommendations (Activity activity);

    RecommendationDto saveNewRecommendation (Recommendation recommendation);

}
