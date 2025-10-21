package com.example.emiraia.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class LLamaService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String llamaUrl = "http://localhost:5001/generate";

    public String generateMessage(String prompt) {
        Map<String, String> body = Map.of("prompt", prompt);
        Map<String, String> response = restTemplate.postForObject(llamaUrl, body, Map.class);
        return response.get("response");
    }
}
