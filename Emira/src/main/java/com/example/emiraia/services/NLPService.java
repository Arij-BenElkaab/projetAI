package com.example.emiraia.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class NLPService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String nlpUrl = "http://localhost:5000/extract";

    public Map<String, Object> parseRequest(String text) {
        Map<String, String> body = Map.of("text", text);
        return restTemplate.postForObject(nlpUrl, body, Map.class);
    }
}
