package com.makers.memoir.controller;

import com.makers.memoir.model.GroupMember;
import com.makers.memoir.model.User;
import com.makers.memoir.model.Weekly;
import com.makers.memoir.repository.GroupMemberRepository;
import com.makers.memoir.repository.UserRepository;
import com.makers.memoir.repository.WeeklyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ArchiveController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupMemberRepository groupMemberRepository;

    @Autowired
    WeeklyRepository weeklyRepository;

    private String getUsernameFromPrincipal(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) principal;
            return token.getPrincipal().getAttribute("email");
        }
        return principal.getName();
    }

    private List<Long> getGroupIds(Principal principal) {
        User currentUser = userRepository.findByEmail(getUsernameFromPrincipal(principal));
        return groupMemberRepository
                .findByUserIdAndStatus(currentUser.getId(), "joined")
                .stream()
                .map(gm -> gm.getGroup().getId())
                .collect(Collectors.toList());
    }

    @GetMapping("/archive")
    public String archive(Model model, Principal principal) {
        List<Long> groupIds = getGroupIds(principal);

        List<Weekly> newsletters = groupIds.isEmpty()
                ? List.of()
                : weeklyRepository.findByGroupIdInAndStatus(groupIds, "sent");

        model.addAttribute("newsletters", newsletters);
        return "archive/index";
    }

    @GetMapping("/archive/{date}")
    public String archiveDay(@PathVariable String date, Model model, Principal principal) {
        List<Long> groupIds = getGroupIds(principal);
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime dayStart = localDate.atStartOfDay();
        LocalDateTime dayEnd = localDate.atTime(23, 59, 59);

        List<Weekly> newsletters = groupIds.isEmpty()
                ? List.of()
                : weeklyRepository.findByGroupIdsAndSentDate(groupIds, dayStart, dayEnd);

        model.addAttribute("newsletters", newsletters);
        model.addAttribute("date", localDate);
        return "archive/day";
    }

    @GetMapping("/newsletters/current")
    public String currentNewsletters(Model model, Principal principal) {
        List<Long> groupIds = getGroupIds(principal);

        List<Weekly> newsletters = groupIds.isEmpty()
                ? List.of()
                : groupIds.stream()
                .map(id -> weeklyRepository
                        .findFirstByGroupIdAndStatusOrderByWeekStartDesc(id, "sent")
                        .orElse(null))
                .filter(w -> w != null)
                .collect(Collectors.toList());

        model.addAttribute("newsletters", newsletters);
        return "newsletters/current";
    }
}