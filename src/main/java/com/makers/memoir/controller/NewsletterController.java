package com.makers.memoir.controller;

import com.cloudinary.Cloudinary;
import com.makers.memoir.model.Group;
import com.makers.memoir.model.GroupMember;
import com.makers.memoir.model.Moment;
import com.makers.memoir.model.User;
import com.makers.memoir.model.Weekly;
import com.makers.memoir.repository.*;
import com.makers.memoir.service.EmailService;
import com.makers.memoir.service.NewsletterService;
import com.makers.memoir.service.PdfService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/newsletter")
public class NewsletterController {

    @Autowired
    NewsletterService newsletterService;

    @Autowired
    MomentRepository momentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    EmailService emailService;

    @Autowired
    PdfService pdfService;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    GroupMemberRepository groupMemberRepository;

    @Autowired
    Cloudinary cloudinary;

    @Autowired
    WeeklyRepository weeklyRepository;

    private String getUsernameFromPrincipal(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) principal;
            return token.getPrincipal().getAttribute("email");
        }
        return principal.getName();
    }

    private List<List<String>> toBase64ImageRows(List<List<String>> imageRows) {
        return imageRows.stream()
                .map(row -> row.stream()
                        .map(url -> pdfService.convertImageUrlToBase64(url))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private void populatePdfContext(Context context, NewsletterData data,
                                    Long groupId, LocalDateTime weekStart) {
        List<List<String>> base64ImageRows = toBase64ImageRows(data.imageRows);
        context.setVariable("summaries", data.summaries);
        context.setVariable("memberMoments", data.memberMoments);
        context.setVariable("allImageUrls", data.allImageUrls);
        context.setVariable("imageRows", base64ImageRows);
        context.setVariable("colWidth", data.colWidth);
        context.setVariable("imageHeight", data.imageHeight);
        context.setVariable("groupSummary", data.groupSummary);
        context.setVariable("groupId", groupId);
        context.setVariable("weekStart", weekStart);
    }

    private void populateWebContext(Model model, NewsletterData data,
                                    Long groupId, LocalDateTime weekStart) {
        model.addAttribute("summaries", data.summaries);
        model.addAttribute("memberMoments", data.memberMoments);
        model.addAttribute("allImageUrls", data.allImageUrls);
        model.addAttribute("imageRows", data.imageRows);
        model.addAttribute("colWidth", data.colWidth);
        model.addAttribute("imageHeight", data.imageHeight);
        model.addAttribute("groupSummary", data.groupSummary);
        model.addAttribute("groupId", groupId);
        model.addAttribute("weekStart", weekStart);
    }

    private NewsletterData buildNewsletterData(Long groupId, LocalDateTime weekStart,
                                               LocalDateTime weekEnd) {
        List<GroupMember> groupMembers = groupMemberRepository
                .findByGroupIdAndStatus(groupId, "joined");
        List<User> members = groupMembers.stream()
                .map(GroupMember::getUser)
                .collect(Collectors.toList());

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
                String summary = newsletterService.generateUserSummary(fullName, firstName, moments);
                summaries.put(member, summary);
                memberMoments.put(member, moments);
                moments.stream()
                        .map(Moment::getImageUrl)
                        .filter(url -> url != null)
                        .forEach(allImageUrls::add);
            }
        }

        Collections.shuffle(allImageUrls);

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

        String groupSummary = summaries.isEmpty()
                ? null
                : newsletterService.generateGroupSummary(summaries);

        return new NewsletterData(summaries, memberMoments, allImageUrls,
                imageRows, colWidth, imageHeight, groupSummary, members);
    }

    private String buildNewsletterHtml(Long groupId, LocalDateTime weekStart,
                                       LocalDateTime weekEnd) {
        NewsletterData data = buildNewsletterData(groupId, weekStart, weekEnd);
        Context context = new Context();
        populatePdfContext(context, data, groupId, weekStart);
        return templateEngine.process("newsletter/pdf", context);
    }

    @GetMapping("/group/{groupId}")
    public String viewNewsletter(@PathVariable Long groupId, Model model,
                                 Principal principal) {
        LocalDateTime weekStart = LocalDateTime.now()
                .with(DayOfWeek.MONDAY)
                .truncatedTo(ChronoUnit.DAYS);
        LocalDateTime weekEnd = weekStart.plusDays(7);

        Group group = groupRepository.findById(groupId).orElseThrow();
        Optional<Weekly> existingWeekly = weeklyRepository
                .findByGroupAndWeekStart(group, weekStart);

        // Serve cached version if it exists
        if (existingWeekly.isPresent() && existingWeekly.get().getHtmlContent() != null) {
            Weekly cached = existingWeekly.get();
            model.addAttribute("cachedHtml", cached.getHtmlContent());
            model.addAttribute("groupId", groupId);
            model.addAttribute("weekStart", weekStart);
            return "newsletter/cached";
        }

        // Generate fresh newsletter
        NewsletterData data = buildNewsletterData(groupId, weekStart, weekEnd);
        populateWebContext(model, data, groupId, weekStart);

        try {
            // Build PDF context with base64 images
            Context pdfContext = new Context();
            populatePdfContext(pdfContext, data, groupId, weekStart);
            String pdfHtml = templateEngine.process("newsletter/pdf", pdfContext);
            byte[] pdfBytes = pdfService.generatePdf(pdfHtml);

            // Build web HTML for caching
            Context webContext = new Context();
            webContext.setVariable("summaries", data.summaries);
            webContext.setVariable("memberMoments", data.memberMoments);
            webContext.setVariable("allImageUrls", data.allImageUrls);
            webContext.setVariable("imageRows", data.imageRows);
            webContext.setVariable("colWidth", data.colWidth);
            webContext.setVariable("imageHeight", data.imageHeight);
            webContext.setVariable("groupSummary", data.groupSummary);
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

            // Save Weekly record with cached HTML and PDF URL
            Weekly weekly = existingWeekly.orElse(
                    new Weekly(group, weekStart, weekStart.plusDays(6)));
            weekly.setStatus("sent");
            weekly.setSentAt(LocalDateTime.now());
            weekly.setPdfUrl(pdfUrl);
            weekly.setHtmlContent(webHtml);
            weeklyRepository.save(weekly);

            // Email sending will be handled by scheduler on release day

        } catch (Exception e) {
            System.err.println("Failed to generate newsletter: " + e.getMessage());
        }

        return "newsletter/index";
    }

    @GetMapping("/group/{groupId}/pdf")
    @ResponseBody
    public ResponseEntity<byte[]> downloadNewsletter(@PathVariable Long groupId,
                                                     Principal principal) {
        LocalDateTime weekStart = LocalDateTime.now()
                .with(DayOfWeek.MONDAY)
                .truncatedTo(ChronoUnit.DAYS);
        LocalDateTime weekEnd = weekStart.plusDays(7);

        try {
            String htmlContent = buildNewsletterHtml(groupId, weekStart, weekEnd);
            byte[] pdfBytes = pdfService.generatePdf(htmlContent);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "newsletter.pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static class NewsletterData {
        Map<User, String> summaries;
        Map<User, List<Moment>> memberMoments;
        List<String> allImageUrls;
        List<List<String>> imageRows;
        int colWidth;
        int imageHeight;
        String groupSummary;
        List<User> members;

        NewsletterData(Map<User, String> summaries, Map<User, List<Moment>> memberMoments,
                       List<String> allImageUrls, List<List<String>> imageRows,
                       int colWidth, int imageHeight, String groupSummary,
                       List<User> members) {
            this.summaries = summaries;
            this.memberMoments = memberMoments;
            this.allImageUrls = allImageUrls;
            this.imageRows = imageRows;
            this.colWidth = colWidth;
            this.imageHeight = imageHeight;
            this.groupSummary = groupSummary;
            this.members = members;
        }
    }
}