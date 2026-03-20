package com.makers.memoir.service;

import com.cloudinary.Cloudinary;
import com.makers.memoir.model.GroupMember;
import com.makers.memoir.model.Moment;
import com.makers.memoir.model.User;
import com.makers.memoir.model.Weekly;
import com.makers.memoir.repository.GroupMemberRepository;
import com.makers.memoir.repository.MomentRepository;
import com.makers.memoir.repository.WeeklyRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class NewsletterScheduler {

    @Autowired
    WeeklyRepository weeklyRepository;

    @Autowired
    MomentRepository momentRepository;

    @Autowired
    GroupMemberRepository groupMemberRepository;

    @Autowired
    NewsletterService newsletterService;

    @Autowired
    PdfService pdfService;

    @Autowired
    EmailService emailService;

    @Autowired
    Cloudinary cloudinary;

    @Autowired
    TemplateEngine templateEngine;

    // Runs every hour
    @Scheduled(cron = "0 0 * * * *")
    public void sendScheduledNewsletters() {
        System.out.println("NewsletterScheduler running at: " + LocalDateTime.now());

        // Find all open weekly records whose send date has passed
        List<Weekly> due = weeklyRepository
                .findByStatusAndSendDateBefore("open", LocalDateTime.now());

        for (Weekly weekly : due) {
            try {
                processNewsletter(weekly);
            } catch (Exception e) {
                System.err.println("Failed to process newsletter for group "
                        + weekly.getGroup().getName() + ": " + e.getMessage());
            }
        }
    }

    private void processNewsletter(Weekly weekly) throws Exception {
        Long groupId = weekly.getGroup().getId();
        LocalDateTime weekStart = weekly.getWeekStart();
        LocalDateTime weekEnd = weekly.getSendDate();

        // Fetch joined members
        List<GroupMember> groupMembers = groupMemberRepository
                .findByGroupIdAndStatus(groupId, "joined");
        List<User> members = groupMembers.stream()
                .map(GroupMember::getUser)
                .collect(Collectors.toList());

        // Build summaries and image data
        Map<User, String> summaries = new LinkedHashMap<>();
        Map<User, List<Moment>> memberMoments = new LinkedHashMap<>();
        List<String> allImageUrls = new ArrayList<>();

        for (User member : members) {
            List<Moment> moments = momentRepository.findByCreatedByIdAndCreatedAtBetween(
                    member.getId(), weekStart, weekEnd
            );

            if (!moments.isEmpty()) {
                String fullName = member.getFirstname() + " " + member.getLastname();
                String firstName = member.getFirstname();
                String summary = newsletterService.generateUserSummary(
                        fullName, firstName, moments
                );
                summaries.put(member, summary);
                memberMoments.put(member, moments);
                moments.stream()
                        .map(Moment::getImageUrl)
                        .filter(url -> url != null)
                        .forEach(allImageUrls::add);
            }
        }

        if (summaries.isEmpty()) {
            System.out.println("No moments for group " + weekly.getGroup().getName()
                    + " — skipping newsletter");
            return;
        }

        Collections.shuffle(allImageUrls);

        // Calculate image grid layout
        int totalImages = allImageUrls.size();
        int bestColumns = 3;
        int bestRemainder = totalImages % 3;
        for (int cols = 2; cols <= 5; cols++) {
            int remainder = totalImages % cols;
            if (remainder == 0 || remainder > bestRemainder) {
                bestColumns = cols;
                bestRemainder = remainder;
            }
        }
        int imageHeight;
        switch (bestColumns) {
            case 2: imageHeight = 200; break;
            case 3: imageHeight = 160; break;
            case 4: imageHeight = 130; break;
            case 5: imageHeight = 110; break;
            default: imageHeight = 150;
        }
        int colWidth = 100 / bestColumns;

        List<List<String>> imageRows = new ArrayList<>();
        for (int i = 0; i < allImageUrls.size(); i += bestColumns) {
            imageRows.add(allImageUrls.subList(i,
                    Math.min(i + bestColumns, allImageUrls.size())));
        }

        String groupSummary = newsletterService.generateGroupSummary(summaries);

        // Build base64 image rows for PDF
        List<List<String>> base64ImageRows = imageRows.stream()
                .map(row -> row.stream()
                        .map(url -> pdfService.convertImageUrlToBase64(url))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        // Build PDF context
        Context pdfContext = new Context();
        pdfContext.setVariable("summaries", summaries);
        pdfContext.setVariable("memberMoments", memberMoments);
        pdfContext.setVariable("allImageUrls", allImageUrls);
        pdfContext.setVariable("imageRows", base64ImageRows);
        pdfContext.setVariable("colWidth", colWidth);
        pdfContext.setVariable("imageHeight", imageHeight);
        pdfContext.setVariable("groupSummary", groupSummary);
        pdfContext.setVariable("groupId", groupId);
        pdfContext.setVariable("weekStart", weekStart);

        String pdfHtml = templateEngine.process("newsletter/pdf", pdfContext);
        byte[] pdfBytes = pdfService.generatePdf(pdfHtml);

        // Build web HTML for caching
        Context webContext = new Context();
        webContext.setVariable("summaries", summaries);
        webContext.setVariable("memberMoments", memberMoments);
        webContext.setVariable("allImageUrls", allImageUrls);
        webContext.setVariable("imageRows", imageRows);
        webContext.setVariable("colWidth", colWidth);
        webContext.setVariable("imageHeight", imageHeight);
        webContext.setVariable("groupSummary", groupSummary);
        webContext.setVariable("groupId", groupId);
        webContext.setVariable("weekStart", weekStart);
        String webHtml = templateEngine.process("newsletter/index", webContext);

        // Upload PDF to Cloudinary
        Map<String, Object> uploadOptions = new HashMap<>();
        uploadOptions.put("resource_type", "raw");
        uploadOptions.put("public_id", "newsletters/group_" + groupId + "_"
                + weekStart.toLocalDate());
        uploadOptions.put("overwrite", true);
        Map pdfUploadResult = cloudinary.uploader().upload(pdfBytes, uploadOptions);
        String pdfUrl = (String) pdfUploadResult.get("secure_url");

        // Update Weekly record
        weekly.setStatus("sent");
        weekly.setSentAt(LocalDateTime.now());
        weekly.setPdfUrl(pdfUrl);
        weekly.setHtmlContent(webHtml);
        weeklyRepository.save(weekly);

        // Email to all joined members
        for (GroupMember gm : groupMembers) {
            try {
                emailService.sendNewsletterEmail(
                        gm.getUser().getEmail(),
                        "Your Weekly Memoir Newsletter — " + weekly.getGroup().getName(),
                        pdfBytes
                );
            } catch (MessagingException e) {
                System.err.println("Failed to email " + gm.getUser().getEmail()
                        + ": " + e.getMessage());
            }
        }

        System.out.println("Newsletter sent for group: " + weekly.getGroup().getName());
    }
}