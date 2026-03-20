//package com.makers.memoir.service;
//
//import com.makers.memoir.model.Moment;
//import com.makers.memoir.model.User;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.ArrayList;
//import java.util.Base64;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Service
//public class NewsletterService {
//
//    @Value("${gemini.api.key}")
//    private String apiKey;
//
//    private final RestTemplate restTemplate = new RestTemplate();
//
////    A helper to ensure that theres no problems with file type
//    private String getMimeType(String imageUrl) {
//        if (imageUrl.endsWith(".png")) return "image/png";
//        if (imageUrl.endsWith(".jpg") || imageUrl.endsWith(".jpeg")) return "image/jpeg";
//        if (imageUrl.endsWith(".webp")) return "image/webp";
//        return "image/jpeg";
//    }
//
////    The input to the LLM cant be an image URL it has to actually be the image in base64
//    private List<Map<String, Object>> buildPartsForMoment(Moment moment) {
//        List<Map<String, Object>> parts = new ArrayList<>();
//
//        if (moment.getImageUrl() != null) {
//            byte[] imageBytes = restTemplate.getForObject(moment.getImageUrl(), byte[].class);
//            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
//            parts.add(Map.of(
//                    "inline_data", Map.of(
//                            "mime_type", getMimeType(moment.getImageUrl()),
//                            "data", base64Image
//                    )
//            ));
//        }
//
//        StringBuilder text = new StringBuilder();
//        if (moment.getContent() != null) {
//            text.append("Description: ").append(moment.getContent());
//        }
//        if (moment.getLocation() != null) {
//            text.append(" | Location: ").append(moment.getLocation());
//        }
//        if (!text.isEmpty()) {
//            parts.add(Map.of("text", text.toString()));
//        }
//
//        return parts;
//    }
//
//    @SuppressWarnings("unchecked")
//    public String generateUserSummary(String username, List<Moment> moments) {
//
//        // Build a content turn per moment plus a final instruction turn at the end
//        List<Map<String, Object>> contents = new ArrayList<>();
//
//        for (Moment moment : moments) {
//            contents.add(Map.of(
//                    "role", "user",
//                    "parts", buildPartsForMoment(moment)
//            ));
//        }
//
//        // Final instruction turn
////         Did some prompt engineering around this
//        contents.add(Map.of(
//                "role", "user",
//                "parts", List.of(Map.of("text",
//                        "You are writing a warm, personal weekly summary for a group memory app. " +
//                                "Based on the moments above captured by " + username + " this week, " +
//                                "write a friendly 3-4 sentence paragraph in third person that captures " +
//                                "the essence of their week in a natural, engaging way. " +
//                                "Weave the images, descriptions and locations together into a flowing narrative — " +
//                                "avoid listing the moments one by one."))
//        ));
//
//        Map<String, Object> requestBody = Map.of("contents", contents);
//
//        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;
//
//        Map<String, Object> response = (Map<String, Object>) restTemplate.postForObject(url, requestBody, Map.class);
//
//        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
//        Map<String, Object> firstCandidate = candidates.get(0);
//        Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
//        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
//        Map<String, Object> firstPart = parts.get(0);
//        return (String) firstPart.get("text");
//    }
//
//    @SuppressWarnings("unchecked")
//    public String generateGroupSummary(Map<User, String> summaries) {
//        try {
//            StringBuilder input = new StringBuilder();
//            for (Map.Entry<User, String> entry : summaries.entrySet()) {
//                input.append(entry.getKey().getUsername())
//                        .append(": ")
//                        .append(entry.getValue())
//                        .append("\n\n");
//            }
//
//            String prompt = "You are writing a warm, uplifting closing paragraph for a group memory newsletter. " +
//                    "Below are the individual weekly summaries for each member of the group. " +
//                    "Write a single 3-4 sentence paragraph that captures the collective spirit of the group's week, " +
//                    "weaving together their shared and individual experiences into one cohesive, " +
//                    "warm and celebratory closing note. Write in third person.\n\n" +
//                    "Member summaries:\n" + input;
//
//            Map<String, Object> requestBody = Map.of(
//                    "contents", List.of(
//                            Map.of("parts", List.of(
//                                    Map.of("text", prompt)
//                            ))
//                    )
//            );
//
//            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;
//
//            Map<String, Object> response = (Map<String, Object>) restTemplate.postForObject(url, requestBody, Map.class);
//
//            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
//            Map<String, Object> firstCandidate = candidates.get(0);
//            Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
//            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
//            Map<String, Object> firstPart = parts.get(0);
//            return (String) firstPart.get("text");
//        } catch (Exception e) {
//            System.err.println("Group summary generation failed: " + e.getMessage());
//            return null; // gracefully return null instead of crashing
//        }
//    }
//}
package com.makers.memoir.service;

import com.makers.memoir.model.Moment;
import com.makers.memoir.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

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
    public String generateUserSummary(String fullName, String firstName, List<Moment> moments) {
        List<Map<String, Object>> contents = new ArrayList<>();

        for (Moment moment : moments) {
            contents.add(Map.of(
                    "role", "user",
                    "parts", buildPartsForMoment(moment)
            ));
        }

        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text",
                        "You are writing a warm, personal weekly summary for a group memory app. " +
                                "Based on the moments above captured by " + fullName + " this week, " +
                                "write a friendly 3-4 sentence paragraph in third person that captures " +
                                "the essence of their week in a natural, engaging way. " +
                                "Refer to them by their first name (" + firstName + ") throughout the narrative. " +
                                "Weave the images, descriptions and locations together into a flowing narrative — " +
                                "avoid listing the moments one by one."))
        ));
        Map<String, Object> requestBody = Map.of("contents", contents);
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;
        Map<String, Object> response = (Map<String, Object>) restTemplate.postForObject(url, requestBody, Map.class);

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        Map<String, Object> firstCandidate = candidates.get(0);
        Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        Map<String, Object> firstPart = parts.get(0);

        // Small delay before returning to give Gemini breathing room
        // before the group summary call
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return (String) firstPart.get("text");
    }

    @SuppressWarnings("unchecked")
    public String generateGroupSummary(Map<User, String> summaries) {
        try {
            StringBuilder input = new StringBuilder();
            for (Map.Entry<User, String> entry : summaries.entrySet()) {
                input.append(entry.getKey().getFirstname())
                        .append(" ")
                        .append(entry.getKey().getLastname())
                        .append(": ")
                        .append(entry.getValue())
                        .append("\n\n");
            }

            String prompt = "You are writing a warm, uplifting closing paragraph for a group memory newsletter. " +
                    "Below are the individual weekly summaries for each member of the group. " +
                    "Write a single 3-4 sentence paragraph that captures the collective spirit of the group's week, " +
                    "weaving together their shared and individual experiences into one cohesive, " +
                    "warm and celebratory closing note. Write in third person.\n\n" +
                    "Member summaries:\n" + input;

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    )
            );

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;
            Map<String, Object> response = (Map<String, Object>) restTemplate.postForObject(url, requestBody, Map.class);

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> firstCandidate = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            Map<String, Object> firstPart = parts.get(0);
            return (String) firstPart.get("text");

        } catch (Exception e) {
            System.err.println("Group summary generation failed: " + e.getMessage());
            return null;
        }
    }
}