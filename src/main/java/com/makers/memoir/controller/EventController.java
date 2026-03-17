package com.makers.memoir.controller;

import com.makers.memoir.model.Event;
import com.makers.memoir.model.Group;
import com.makers.memoir.model.Moment;
import com.makers.memoir.model.User;
import com.makers.memoir.repository.EventMomentRepository;
import com.makers.memoir.repository.EventRepository;
import com.makers.memoir.repository.GroupRepository;
import com.makers.memoir.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

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

        ModelAndView mav = new ModelAndView("events/index");
        mav.addObject("events", events);
        mav.addObject("groupId", groupId);
        return mav;
    }

    // Show a single event and its moments
    @GetMapping("/{id}")
    public ModelAndView show(@PathVariable Long groupId,
                             @PathVariable Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        List<Moment> moments = eventMomentRepository.findMomentsByEventId(id);

        ModelAndView mav = new ModelAndView("events/show");
        mav.addObject("event", event);
        mav.addObject("moments", moments);
        return mav;
    }

    // Show create event form
    @GetMapping("/new")
    public ModelAndView newEvent(@PathVariable Long groupId) {
        ModelAndView mav = new ModelAndView("events/new");
        mav.addObject("event", new Event());
        mav.addObject("groupId", groupId);
        return mav;
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

        ModelAndView mav = new ModelAndView("events/edit");
        mav.addObject("event", event);
        mav.addObject("groupId", groupId);
        return mav;
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