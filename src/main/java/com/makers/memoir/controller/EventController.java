package com.makers.memoir.controller;

import com.makers.memoir.model.Event;
import com.makers.memoir.model.Group;
import com.makers.memoir.model.Moment;
import com.makers.memoir.model.User;
import com.makers.memoir.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/groups/{groupId}/events")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventMomentRepository eventMomentRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    // List all events for a group
    @GetMapping
    public ModelAndView index(@PathVariable Long groupId) {
        List<Event> events = eventRepository.findByGroupIdOrderByStartDateDesc(groupId);

        ModelAndView modelAndView = new ModelAndView("events/index");
        modelAndView.addObject("events", events);
        modelAndView.addObject("groupId", groupId);
        return modelAndView;
    }


    @GetMapping("/{id}")
    public ModelAndView show(@PathVariable Long groupId,
                             @PathVariable Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        List<Moment> moments = eventMomentRepository.findMomentsByEventId(id);

        // Flat map for JS serialization
        List<Map<String, Object>> momentMaps = moments.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("imageUrl", m.getImageUrl());
            map.put("content", m.getContent());
            map.put("location", m.getLocation());
            map.put("latitude", m.getLatitude());
            map.put("longitude", m.getLongitude());
            map.put("author", m.getCreatedBy().getFirstname());
            map.put("date", m.getCreatedAt() != null
                    ? m.getCreatedAt().format(DateTimeFormatter.ofPattern("d MMM yyyy"))
                    : "");
            return map;
        }).toList();

        ModelAndView modelAndView = new ModelAndView("events/show");
        modelAndView.addObject("event", event);
        modelAndView.addObject("moments", moments);
        modelAndView.addObject("momentMaps", momentMaps);
        modelAndView.addObject("groupId", groupId);
        modelAndView.addObject("pageTitle", event.getName());
        return modelAndView;
    }

    // Show create event form
    @GetMapping("/new")
    public ModelAndView newEvent(@PathVariable Long groupId) {
        ModelAndView modelAndView = new ModelAndView("events/new");
        modelAndView.addObject("event", new Event());
        modelAndView.addObject("groupId", groupId);
        return modelAndView;
    }

    // Create a new event
    @PostMapping
    public ModelAndView create(@PathVariable Long groupId,
                               @AuthenticationPrincipal OAuth2User principal,
                               @ModelAttribute Event event) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User user = getCurrentUser(principal);
        event.setGroup(group);
        event.setCreatedBy(user);
        eventRepository.save(event);

        return new ModelAndView("redirect:/groups/" + groupId + "/events/" + event.getId());
    }

    // Show edit form
    @GetMapping("/{id}/edit")
    public ModelAndView edit(@PathVariable Long groupId,
                             @PathVariable Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        ModelAndView modelAndView = new ModelAndView("events/edit");
        modelAndView.addObject("event", event);
        modelAndView.addObject("groupId", groupId);
        return modelAndView;
    }

    // Update an event
    @PostMapping("/{id}/edit")
    public ModelAndView update(@PathVariable Long groupId,
                               @PathVariable Long id,
                               @ModelAttribute Event updatedEvent) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        event.setName(updatedEvent.getName());
        event.setDescription(updatedEvent.getDescription());
        event.setStartDate(updatedEvent.getStartDate());
        event.setEndDate(updatedEvent.getEndDate());
        event.setLocation(updatedEvent.getLocation());
        eventRepository.save(event);

        return new ModelAndView("redirect:/groups/" + groupId + "/events/" + id);
    }

    // Delete an event
    @PostMapping("/{id}/delete")
    public ModelAndView delete(@PathVariable Long groupId,
                               @PathVariable Long id) {
        eventRepository.deleteById(id);
        return new ModelAndView("redirect:/groups/" + groupId + "/events");
    }

    private User getCurrentUser(OAuth2User principal) {
        String email = principal.getAttribute("email");
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}