package com.makers.memoir.service;

import com.makers.memoir.model.Moment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NewsletterService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private String getMimeType(String imageUrl) {
        if (imageUrl.endsWith(".png")) return "image/png";
        if (imageUrl.endsWith(".jpg") || imageUrl.endsWith(".jpeg")) return "image/jpeg";
        if (imageUrl.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    private List<Map<String, Object>> buildPartsForMoment(Moment moment) {
        List<Map<String, Object>> parts = new ArrayList<>();

        if (moment.getImageUrl() != null) {
            byte[] imageBytes = restTemplate.getForObject(moment.getImageUrl(), byte[].class);
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            parts.add(Map.of(
                    "inline_data", Map.of(
                            "mime_type", getMimeType(moment.getImageUrl()),
                            "data", base64Image
                    )
            ));
        }

        StringBuilder text = new StringBuilder();
        if (moment.getContent() != null) {
            text.append("Description: ").append(moment.getContent());
        }
        if (moment.getLocation() != null) {
            text.append(" | Location: ").append(moment.getLocation());
        }
        if (!text.isEmpty()) {
            parts.add(Map.of("text", text.toString()));
        }

        return parts;
    }

    @SuppressWarnings("unchecked")
    public String generateUserSummary(String username, List<Moment> moments) {

        // Build a content turn per moment plus a final instruction turn
        List<Map<String, Object>> contents = new ArrayList<>();

        for (Moment moment : moments) {
            contents.add(Map.of(
                    "role", "user",
                    "parts", buildPartsForMoment(moment)
            ));
        }

        // Final instruction turn
        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text",
                        "You are writing a warm, personal weekly summary for a group memory app. " +
                                "Based on the moments above captured by " + username + " this week, " +
                                "write a friendly 3-4 sentence paragraph in third person that captures " +
                                "the essence of their week in a natural, engaging way. " +
                                "Weave the images, descriptions and locations together into a flowing narrative — " +
                                "avoid listing the moments one by one."))
        ));

        Map<String, Object> requestBody = Map.of("contents", contents);

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