package com.makers.memoir.controller;

import com.makers.memoir.model.Moment;
import com.makers.memoir.model.User;
import com.makers.memoir.repository.MomentRepository;
import com.makers.memoir.repository.UserRepository;
import com.makers.memoir.service.NewsletterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/newsletter")
public class NewsletterController {

    @Autowired
    NewsletterService newsletterService;

    @Autowired
    MomentRepository momentRepository;

    @Autowired
    UserRepository userRepository;

    // TODO: Autowire these once Mark's work is merged
    // @Autowired
    // GroupRepository groupRepository;
    // @Autowired
    // GroupMemberRepository groupMemberRepository;

//    Currently this will all error since the login and OAuth hasnt been setup
    private String getUsernameFromPrincipal(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) principal;
            return token.getPrincipal().getAttribute("email");
        }
        return principal.getName();
    }

    @GetMapping("/group/{groupId}")
    public String viewNewsletter(@PathVariable Long groupId, Model model, Principal principal) {

        // Work out the start and end of the current week (Monday to Sunday)
        LocalDateTime weekStart = LocalDateTime.now()
                .with(DayOfWeek.MONDAY)
                .truncatedTo(ChronoUnit.DAYS);
        LocalDateTime weekEnd = weekStart.plusDays(7);

        // TODO: Replace this with real group member fetching once Mark's work is merged
        // List<GroupMember> groupMembers = groupMemberRepository
        //         .findByGroupIdAndStatus(groupId, "joined");
        // List<User> members = groupMembers.stream()
        //         .map(GroupMember::getUser)
        //         .collect(Collectors.toList());

        // Placeholder
        User currentUser = userRepository.findByEmail(getUsernameFromPrincipal(principal));
        List<User> members = List.of(currentUser);

        // Build a summary per member
        Map<User, String> summaries = new LinkedHashMap<>();
        Map<User, List<Moment>> memberMoments = new LinkedHashMap<>();

        for (User member : members) {
            List<Moment> moments = momentRepository.findByCreatedByIdAndCreatedAtBetween(
                    member.getId(), weekStart, weekEnd
            );

            // Only include members who actually posted something this week
            if (!moments.isEmpty()) {
                String summary = newsletterService.generateUserSummary(
                        member.getUsername(), moments
                );
                summaries.put(member, summary);
                memberMoments.put(member, moments);
            }
        }

        model.addAttribute("summaries", summaries);
        model.addAttribute("memberMoments", memberMoments);
        model.addAttribute("groupId", groupId);
        model.addAttribute("weekStart", weekStart);

        return "newsletter/index";
    }
}