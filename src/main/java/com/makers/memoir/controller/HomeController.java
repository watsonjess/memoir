package com.makers.memoir.controller;

import com.makers.memoir.model.*;
import com.makers.memoir.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Controller for the home page.
 */
@Controller
public class HomeController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private WeeklyRepository weeklyRepository;

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal OidcUser principal) {
        if(principal != null){
            User currentUser = userRepository.findByEmail(principal.getEmail());

            if (currentUser != null) {
                model.addAttribute("firstName", currentUser.getFirstname());

                List<Moment> moments = momentRepository.findByCreatedByIdOrderByCreatedAtDesc(currentUser.getId());
                model.addAttribute("moments", moments);

                List<GroupMember> userGroups = groupMemberRepository.findByUserId(currentUser.getId());
                model.addAttribute("userGroups", userGroups);

                List<Event> events = userGroups.stream()
                        .filter(g -> g.getStatus().equals("joined"))
                        .flatMap(g -> eventRepository.findByGroupId(g.getGroup().getId()).stream())
                        .filter(e -> e.getStartDate() != null && e.getStartDate().isAfter(LocalDateTime.now()))
                        .sorted(Comparator.comparing(Event::getStartDate))
                        .limit(3)
                        .toList();
                model.addAttribute("events", events);

                List<Weekly> newsletters = userGroups.stream()
                        .filter(g -> g.getStatus().equals("joined"))
                        .map(g -> weeklyRepository.findFirstByGroupIdAndStatusOrderByWeekStartDesc(g.getGroup().getId(), "sent")
                                .orElse(null))
                        .filter(Objects::nonNull)
                        .limit(4)
                        .toList();
                model.addAttribute("newsletters", newsletters);


            } else {
                model.addAttribute("moments", new ArrayList<>());
                model.addAttribute("userGroups", new ArrayList<>());
                model.addAttribute("events", new ArrayList<>());
                model.addAttribute("newsletters", new ArrayList<>());
            }
        }

        return "index";
    }
}