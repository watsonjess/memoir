package com.makers.memoir.service;

import com.makers.memoir.model.Moment;
import com.makers.memoir.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class NewsletterServiceTest {

    @Autowired
    NewsletterService newsletterService;

    @Test
    public void testGenerateUserSummary() {
        // Build a fake user
        User user = new User();
        user.setUsername("reece");

        // Build some fake moments with real Cloudinary sample images
        Moment m1 = new Moment();
        m1.setImageUrl("https://res.cloudinary.com/demo/image/upload/sample.jpg");
        m1.setContent("Went out to a flower garden");
        m1.setLocation("London");
        m1.setCreatedAt(LocalDateTime.now());

        Moment m2 = new Moment();
        m2.setImageUrl("https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?w=800");
        m2.setContent("Practiced my camera work with drone shots of london");
        m2.setLocation("Hyde Park");
        m2.setCreatedAt(LocalDateTime.now());

        Moment m3 = new Moment();
        m3.setImageUrl("https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=800");
        m3.setContent("Decided to go walk Hugo through the forest to reconnect with nature, he loved it! ");
        m3.setLocation("Home");
        m3.setCreatedAt(LocalDateTime.now());

        Moment m4 = new Moment();
        m4.setImageUrl("https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800");
        m4.setContent("Caught up with family over dinner");
        m4.setLocation("Shoreditch");
        m4.setCreatedAt(LocalDateTime.now());

        List<Moment> moments = List.of(m1, m2, m3, m4);

        String summary = newsletterService.generateUserSummary("reece", moments);

        System.out.println("=== GENERATED SUMMARY ===");
        System.out.println(summary);
        System.out.println("=========================");
    }
}