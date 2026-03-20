package com.makers.memoir.controller;

import com.makers.memoir.model.Event;
import com.makers.memoir.model.User;
import com.makers.memoir.repository.EventRepository;
import com.makers.memoir.repository.GroupMemberRepository;
import com.makers.memoir.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class EventOverviewController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/events")
    public ModelAndView allEvents(@AuthenticationPrincipal OAuth2User principal) {
        User user = getCurrentUser(principal);

        List<Event> events = groupMemberRepository
                .findByUserId(user.getId())
                .stream()
                .filter(m -> m.getStatus().equals("joined"))
                .flatMap(m -> eventRepository.findByGroupId(m.getGroup().getId()).stream())
                .toList();

        // Map to simple objects safe for JS serialization
        List<Map<String, Object>> eventMaps = events.stream()
                .filter(e -> e.getLatitude() != null && e.getLongitude() != null)
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", e.getId());
                    map.put("name", e.getName());
                    map.put("latitude", e.getLatitude());
                    map.put("longitude", e.getLongitude());
                    map.put("location", e.getLocation());
                    map.put("groupId", e.getGroup().getId());
                    map.put("groupName", e.getGroup().getName());
                    map.put("startDate", e.getStartDate() != null ? e.getStartDate().toString() : null);
                    map.put("endDate", e.getEndDate() != null ? e.getEndDate().toString() : null);
                    return map;
                })
                .toList();

        ModelAndView modelAndView = new ModelAndView("events/overview");
        modelAndView.addObject("events", events);
        modelAndView.addObject("eventMaps", eventMaps);
        modelAndView.addObject("pageTitle", "Events");
        return modelAndView;
    }

    private User getCurrentUser(OAuth2User principal) {
        String email = principal.getAttribute("email");
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}