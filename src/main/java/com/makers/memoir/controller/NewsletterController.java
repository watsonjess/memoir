package com.makers.memoir.controller;

import com.makers.memoir.model.GroupMember;
import com.makers.memoir.model.Moment;
import com.makers.memoir.model.User;
import com.makers.memoir.repository.GroupMemberRepository;
import com.makers.memoir.repository.GroupRepository;
import com.makers.memoir.repository.MomentRepository;
import com.makers.memoir.repository.UserRepository;
import com.makers.memoir.service.EmailService;import com.makers.memoir.service.NewsletterService;
import com.makers.memoir.service.PdfService;import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.mail.MessagingException;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;

import org.thymeleaf.context.Context;
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.ArrayList;
import org.thymeleaf.TemplateEngine;

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

//    Currently this will all error since the login and OAuth hasnt been setup
    private String getUsernameFromPrincipal(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) principal;
            return token.getPrincipal().getAttribute("email");
        }
        return principal.getName();
    }

    private String buildNewsletterHtml(Long groupId, LocalDateTime weekStart, LocalDateTime weekEnd) {
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
                String summary = newsletterService.generateUserSummary(
                        member.getUsername(), moments
                );
                summaries.put(member, summary);
                memberMoments.put(member, moments);
                moments.stream()
                        .map(Moment::getImageUrl)
                        .filter(url -> url != null)
                        .forEach(allImageUrls::add);
            }
        }

        Collections.shuffle(allImageUrls);

        String groupSummary = summaries.isEmpty()
                ? null
                : newsletterService.generateGroupSummary(summaries);

        Context context = new Context();
        context.setVariable("summaries", summaries);
        context.setVariable("memberMoments", memberMoments);
        context.setVariable("allImageUrls", allImageUrls);
        context.setVariable("groupSummary", groupSummary);
        context.setVariable("groupId", groupId);
        context.setVariable("weekStart", weekStart);

        return templateEngine.process("newsletter/index", context);
    }

    @GetMapping("/group/{groupId}")
    public String viewNewsletter(@PathVariable Long groupId, Model model, Principal principal) {
        LocalDateTime weekStart = LocalDateTime.now()
                .with(DayOfWeek.MONDAY)
                .truncatedTo(ChronoUnit.DAYS);
        LocalDateTime weekEnd = weekStart.plusDays(7);

        String htmlContent = buildNewsletterHtml(groupId, weekStart, weekEnd);

        // Add attributes to model for Thymeleaf rendering
        model.addAttribute("groupId", groupId);
        model.addAttribute("weekStart", weekStart);

        // Generate and email PDF
        try {
            byte[] pdfBytes = pdfService.generatePdf(htmlContent);
            List<GroupMember> groupMembers = groupMemberRepository
                    .findByGroupIdAndStatus(groupId, "joined");
            for (GroupMember gm : groupMembers) {
                try {
                    emailService.sendNewsletterEmail(
                            gm.getUser().getEmail(),
                            "Your Weekly Memoir Newsletter",
                            pdfBytes
                    );
                } catch (MessagingException e) {
                    System.err.println("Failed to send email to " + gm.getUser().getEmail() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to generate PDF: " + e.getMessage());
        }

        return "newsletter/index";
    }

    @GetMapping("/group/{groupId}/pdf")
    @ResponseBody
    public ResponseEntity<byte[]> downloadNewsletter(@PathVariable Long groupId, Principal principal) {
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
}