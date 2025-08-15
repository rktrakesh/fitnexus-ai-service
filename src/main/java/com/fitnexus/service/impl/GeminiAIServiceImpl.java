package com.fitnexus.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitnexus.repo.RecommendationRepository;
import com.fitnexus.dto.RecommendationDto;
import com.fitnexus.mapper.RecommendationMapper;
import com.fitnexus.model.Activity;
import com.fitnexus.model.Recommendation;
import com.fitnexus.service.GeminiAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class GeminiAIServiceImpl implements GeminiAIService {

    private final WebClient webClient;

    @Value("${gemini.api.url}")
    private String url;

    @Value("${gemini.api.key}")
    private String key;

    public GeminiAIServiceImpl (WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Override
    public Recommendation generateRecommendations (Activity activity) {
        try {
            String prompt = getAIRecommendationsOnActivity(activity);
            Map <String, Object> requestBody = Map.of(
                    "contents", new Object[] {
                            Map.of(
                                    "parts", new Object[] {
                                            Map.of(
                                                    "text", prompt
                                            )
                                    }
                            )
                    }
            );

            String aiResponse = webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .header("X-goog-api-key", key)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return processAiResponse(activity, aiResponse);
        } catch (Exception e) {
            log.error("Exception wile generateRecommendations: {}", e.getMessage());
            return getDefaultResponse(activity);
        }
    }

    @Override
    public RecommendationDto saveNewRecommendation(Recommendation recommendation) {
        Recommendation saved = recommendationRepository.save(recommendation);
        return RecommendationMapper.mapToDto(saved);
    }

    private Recommendation processAiResponse (Activity activity, String aiResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(aiResponse);
            JsonNode textNode = jsonNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            String recommendationsString = textNode.asText()
                    .replaceAll("```json\\n", "")
                    .replaceAll("\\n```", "")
                    .trim();

            JsonNode recommendationsJson = mapper.readTree(recommendationsString);
            JsonNode analysisNode = recommendationsJson.path("analysis");

            log.info(recommendationsString);

            StringBuilder fullRecommendations = new StringBuilder();
            addAnalysInFullRecommendations(fullRecommendations, analysisNode, "overall", "Overall Analysis: ");
            addAnalysInFullRecommendations(fullRecommendations, analysisNode, "pace", "Pace: ");
            addAnalysInFullRecommendations(fullRecommendations, analysisNode, "heartRate", "Heart Rate: ");
            addAnalysInFullRecommendations(fullRecommendations, analysisNode, "caloriesBurned", "Calories: ");

            List<String> improvements = extractImprovements(recommendationsJson.path("improvements"));
            List<String> suggestions = extractSuggestions(recommendationsJson.path("suggestions"));
            List<String> safety = extractSafety(recommendationsJson.path("safety"));

            return Recommendation.builder()
                    .userId(activity.getUserId())
                    .activityId(activity.getId())
                    .activityType(activity.getType())
                    .recommendations(fullRecommendations.toString().trim())
                    .improvements(improvements)
                    .suggestions(suggestions)
                    .safety(safety)
                    .build();

        } catch (Exception e) {
            log.error("Exception wile getting ai recommendation response.");
            return getDefaultResponse(activity);
        }
    }

    private Recommendation getDefaultResponse(Activity activity) {
        return Recommendation.builder()
                .userId(activity.getUserId())
                .activityId(activity.getId())
                .activityType(activity.getType())
                .recommendations("Unable to find recommendations.")
                .improvements(Collections.singletonList("Continue with the current workout."))
                .suggestions(Collections.singletonList("Consider suggestion from your personal diet manager."))
                .safety(Arrays.asList(
                        "Always warm up before exercise",
                        "Stay Hydrated."
                ))
                .build();

    }

    private List<String> extractSafety(JsonNode safetyNode) {
        List<String> safety = new ArrayList<>();
        if (safetyNode.isArray()) {
            safetyNode.forEach(item -> safety.add(item.asText()));
        }
        return safety.isEmpty()
                ? Collections.singletonList("Follow general safety guidelines.")
                : safety;
    }

    private List<String> extractSuggestions(JsonNode suggestionsNode) {
        List <String> suggestions = new ArrayList<>();
        if (suggestionsNode.isArray()) {
            suggestionsNode.forEach(suggestion -> {
                String workout = suggestion.path("workout").asText();
                String description = suggestion.path("description").asText();
                suggestions.add(String.format("%s, %s", workout, description));
            });
        }
        return suggestions.isEmpty()
                ? Collections.singletonList("No specific suggestions provides.")
                : suggestions;
    }

    private List<String> extractImprovements(JsonNode improvementsNode) {
        List<String> improvements = new ArrayList<>();
        if (improvementsNode.isArray()) {
            improvementsNode.forEach(improvement -> {
                String area = improvement.path("area").asText();

                List<String> recs = new ArrayList<>();
                improvement.path("recommendations").forEach(rec -> recs.add(rec.asText()));
                improvements.add(String.format("%s: %s", area, String.join(", ",recs)));
            });
        }
        return improvements.isEmpty() 
                ? Collections.singletonList("No specific improvements provides.") 
                : improvements;
    }

    private void addAnalysInFullRecommendations(StringBuilder fullRecommendations, JsonNode analysisNode, String key, String prefix) {
        fullRecommendations.append(prefix)
                .append(analysisNode.path(key).asText());
    }

    private String getAIRecommendationsOnActivity(Activity activity) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy,M,d,H,m");

        String startTime = activity.getStartTime() != null
                ? activity.getStartTime().format(formatter)
                : "null";

        StringBuilder additionalMetricsBuilder = new StringBuilder();
        Map<String, Object> additional = activity.getAdditionalMatrics();
        if (additional != null && !additional.isEmpty()) {
            int i = 0;
            for (Map.Entry<String, Object> entry : additional.entrySet()) {
                additionalMetricsBuilder.append(
                        String.format("    \"%s\": %s%s\n",
                                entry.getKey(),
                                entry.getValue(),
                                (++i < additional.size()) ? "," : "")
                );
            }
        }

        return String.format(
                """
                You are a health and fitness assistant. Analyze the following activity data and respond ONLY in valid JSON format (no markdown, no explanation outside JSON).
            
                The JSON structure must look like this:
                {
                  "analysis": {
                    "overall": "<minimum 4 to 8 lines, detailed summary of overall workout performance>",
                    "pace": "<minimum 2 lines of pace analysis>",
                    "heartRate": "<minimum 2 lines of heart rate analysis>",
                    "caloriesBurned": "<minimum 2 lines of calorie burn analysis>"
                  },
                  "improvements": [
                    {
                      "area": "<metric or aspect to improve>",
                      "recommendations": ["<recommendation 1>", "<recommendation 2>", "..."]
                    }
                  ],
                  "suggestions": {
                    "workout": "<type of workout or training style to consider>",
                    "description": "<why this workout is recommended and what it would improve>"
                  },
                  "safety": "<"<safety 1>", "<safety 2>", "..." precautions, form tips, and recovery advice>"
                }
            
                Requirements:
                - All analysis sections must meet the minimum line count
                - Use the provided activity data as the only source for recommendations
                - Keep the tone supportive and practical
                - Do not write anything outside the JSON object
            
                Here is the activity DTO data:
                {
                  "type": "%s",
                  "duration": %d,
                  "caloriesBurned": %d,
                  "startTime": [%s],
                  "additionalMatrics": {
                %s
                  }
                }
                """,
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                startTime,
                additionalMetricsBuilder.toString().trim()
        );

    }
}
