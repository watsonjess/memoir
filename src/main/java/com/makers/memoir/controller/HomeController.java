package com.makers.memoir.controller;

import com.makers.memoir.model.*;
import com.makers.memoir.repository.*;
import com.makers.memoir.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private StatusService statusService;

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal OidcUser principal, @RequestParam(defaultValue = "0") int page) {
        if (principal == null) {
            return "landing";
        }
        if (principal != null) {
            User currentUser = userRepository.findByEmail(principal.getEmail());

            if (currentUser != null) {
                model.addAttribute("firstName", currentUser.getFirstname());

                LocalDateTime end = LocalDateTime.now();
                LocalDateTime start = end.minusDays(7);

                Page<Moment> momentPage = momentRepository
                        .findByCreatedByIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                                currentUser.getId(),
                                start,
                                end,
                                PageRequest.of(page, 2)
                        );

                model.addAttribute("moments", momentPage.getContent());
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", momentPage.getTotalPages());
                model.addAttribute("hasNext", momentPage.hasNext());
                model.addAttribute("hasPrev", momentPage.hasPrevious());

                int allTimeScore = statusService.calculateAllTimeMomentPosts(currentUser);
                model.addAttribute("activityPercentage", allTimeScore);


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

                LocalDateTime todayStart = LocalDate.now().atStartOfDay();
                LocalDateTime todayEnd = todayStart.plusDays(1);

                List<Moment> todayMoments = momentRepository.findByCreatedByIdAndCreatedAtBetween(currentUser.getId(), todayStart, todayEnd);

                Set<Long> groupsPostedToday = todayMoments.stream()
                        .flatMap(m -> m.getGroups().stream())
                        .map(Group::getId)
                        .collect(Collectors.toSet());

                List<Group> groupsMissingToday = userGroups.stream()
                        .filter(g -> g.getStatus().equals("joined"))
                        .map(GroupMember::getGroup)
                        .filter(g -> !groupsPostedToday.contains(g.getId()))
                        .toList();

                model.addAttribute("groupsMissingToday", groupsMissingToday);


            } else {
                model.addAttribute("moments", new ArrayList<>());
                model.addAttribute("userGroups", new ArrayList<>());
                model.addAttribute("events", new ArrayList<>());
                model.addAttribute("newsletters", new ArrayList<>());
                model.addAttribute("groupsMissingToday", new ArrayList<>());
            }
        }

        return "index";
    }
}