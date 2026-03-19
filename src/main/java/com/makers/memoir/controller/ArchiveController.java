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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/archive")
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

    @GetMapping
    public String archive(Model model, Principal principal) {
        User currentUser = userRepository.findByEmail(getUsernameFromPrincipal(principal));

        List<Long> groupIds = groupMemberRepository
                .findByUserIdAndStatus(currentUser.getId(), "joined")
                .stream()
                .map(gm -> gm.getGroup().getId())
                .collect(Collectors.toList());

        List<Weekly> newsletters = groupIds.isEmpty()
                ? List.of()
                : weeklyRepository.findByGroupIdInAndStatus(groupIds, "sent");

        model.addAttribute("newsletters", newsletters);
        return "archive/index";
    }
}