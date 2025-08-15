package com.fitnexus.controller;

import com.fitnexus.dto.RecommendationDto;
import com.fitnexus.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/getRecommendation/{userId}")
    public ResponseEntity<List<RecommendationDto>> getRecommendationsByUserId (@PathVariable String userId) {
        return ResponseEntity.ok(recommendationService.getRecommendationsByUserId(userId));
    }

    @GetMapping("/getRecommendationByActivityId/{activityId}")
    public ResponseEntity<RecommendationDto> getRecommendationsByActivityId (@PathVariable String activityId) {
        return ResponseEntity.ok(recommendationService.getRecommendationsByActivityId(activityId));
    }

}
