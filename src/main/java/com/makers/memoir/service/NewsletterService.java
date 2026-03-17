package com.makers.memoir.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class NewsletterService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateUserSummary(String username, List<String> captions) {
        String prompt = "You are writing a warm, personal weekly summary for a group memory app. " +
                "Below are the daily moment captions submitted by " + username + " this week. " +
                "Write a friendly 3-4 sentence paragraph in third person that captures " +
                "the essence of their week in a natural, engaging way. " +
                "Avoid listing the moments one by one - weave them together into a flowing narrative.\n\n" +
                "Moments:\n" + String.join("\n", captions);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        Map<String, Object> response = (Map<String, Object>) restTemplate.postForObject(url, requestBody, Map.class);

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        Map<String, Object> firstCandidate = candidates.get(0);
        Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        Map<String, Object> firstPart = parts.get(0);
        return (String) firstPart.get("text");
    }
}
