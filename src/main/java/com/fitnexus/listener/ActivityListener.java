package com.fitnexus.listener;

import com.fitnexus.model.Activity;
import com.fitnexus.model.Recommendation;
import com.fitnexus.service.GeminiAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityListener {

    private final GeminiAIService geminiAIService;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void activityMessageListener (Activity activity) {
        log.info("Activity processing starts for the id: {}", activity.getId());
        Recommendation recommendation = geminiAIService.generateRecommendations(activity);
        geminiAIService.saveNewRecommendation(recommendation);
    }

}
