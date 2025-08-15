package com.fitnexus.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations/healthCheck")
public class HealthCheck {

    @GetMapping
    public String healthCheck () {
        return "Health check: AI Service working fine.";
    }

}