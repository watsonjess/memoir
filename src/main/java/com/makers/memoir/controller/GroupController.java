package com.makers.memoir.controller;

import com.makers.memoir.model.*;
import com.makers.memoir.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.LinkedHashMap;

@Controller
@RequestMapping("/groups")
public class GroupController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private WeeklyRepository weeklyRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MomentRepository momentRepository;

    // List all groups the current user belongs to
    @GetMapping
    public ModelAndView index(@AuthenticationPrincipal OAuth2User principal) {
        User user = getCurrentUser(principal);
        List<GroupMember> memberships = groupMemberRepository.findByUserId(user.getId());

        ModelAndView modelAndView = new ModelAndView("groups/index");
        modelAndView.addObject("pageTitle", "Groups");
        modelAndView.addObject("memberships", memberships);
        return modelAndView;
    }

    // Show a single group
    @GetMapping("/{id}")
    public ModelAndView show(@PathVariable Long id,
                             @RequestParam(required = false) String query,
                             @AuthenticationPrincipal OAuth2User principal) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        List<GroupMember> members = groupMemberRepository.findByGroupIdAndStatus(id, "joined");

        ModelAndView modelAndView = new ModelAndView("groups/show");
        modelAndView.addObject("group", group);
        modelAndView.addObject("members", members);
        User user = getCurrentUser(principal);
        Optional<GroupMember> currentMembership = groupMemberRepository
                .findByGroupIdAndUserId(id, user.getId());
        List<GroupMember> pendingMembers = groupMemberRepository
                .findByGroupIdAndStatus(id, "pending");

        modelAndView.addObject("currentMembership", currentMembership.orElse(null));
        modelAndView.addObject("pendingMembers", pendingMembers);

        if (group.getType().equals("weekly")) {
            weeklyRepository.findFirstByGroupIdAndStatusOrderByWeekStartDesc(id, "open")
                    .ifPresent(w -> modelAndView.addObject("currentWeekly", w));
        } else {
            modelAndView.addObject("events", eventRepository.findByGroupIdOrderByStartDateDesc(id));
        }
        return modelAndView;
    }

    // Show create group form
    @GetMapping("/new")
    public ModelAndView newGroup() {
        ModelAndView modelAndView = new ModelAndView("groups/new");
        modelAndView.addObject("group", new Group());
        modelAndView.addObject("pageTitle", "New Group");
        return modelAndView;
    }

    // Create a new group
    @PostMapping
    public ModelAndView create(@AuthenticationPrincipal OAuth2User principal,
                               @ModelAttribute Group group,
                               @RequestParam(required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                               LocalDateTime sendDate,
                               @RequestParam(required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                               LocalDateTime startDate,
                               @RequestParam(required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                               LocalDateTime endDate,
                               @RequestParam(required = false) String location,
                               @RequestParam(required = false) Double latitude,
                               @RequestParam(required = false) Double longitude) {

        User user = getCurrentUser(principal);
        group.setCreatedBy(user);
        groupRepository.save(group);

        GroupMember ownerMembership = new GroupMember(group, user, "owner");
        ownerMembership.setStatus("joined");
        groupMemberRepository.save(ownerMembership);

        if (group.getType().equals("weekly") && sendDate != null) {
            Weekly weekly = new Weekly();
            weekly.setGroup(group);
            weekly.setWeekStart(LocalDateTime.now());
            weekly.setSendDate(sendDate);
            weekly.setStatus("open");
            weeklyRepository.save(weekly);
        } else if (group.getType().equals("event") && startDate != null) {
            Event event = new Event();
            event.setGroup(group);
            event.setName(group.getName());
            event.setStartDate(startDate);
            event.setEndDate(endDate);
            event.setCreatedBy(user);
            event.setLocation(location);
            event.setLatitude(latitude);
            event.setLongitude(longitude);
            eventRepository.save(event);
        }

        if (group.getType().equals("event") && latitude == null) {
            ModelAndView modelAndView = new ModelAndView("groups/new");
            modelAndView.addObject("group", group);
            modelAndView.addObject("errorMessage", "A location is required for event groups.");
            return modelAndView;
        }

        return new ModelAndView("redirect:/groups/" + group.getId());
    }

    // Invite a user to a group by user id
    @PostMapping("/{id}/invite")
    public ModelAndView invite(@PathVariable Long id,
                               @RequestParam Long userId) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User invitee = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean alreadyMember = groupMemberRepository
                .findByGroupIdAndUserId(id, userId).isPresent();
        if (!alreadyMember) {
            GroupMember membership = new GroupMember(group, invitee, "member");
            groupMemberRepository.save(membership);
        }

        return new ModelAndView("redirect:/groups/" + id);
    }

    @GetMapping("/{id}/invite")
    public ModelAndView inviteGet(@PathVariable Long id,
                                  @RequestParam Long userId) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User invitee = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean alreadyMember = groupMemberRepository
                .findByGroupIdAndUserId(id, userId).isPresent();
        if (!alreadyMember) {
            GroupMember membership = new GroupMember();
            membership.setGroup(group);
            membership.setUser(invitee);
            membership.setRole("member");
            membership.setStatus("pending");
            groupMemberRepository.save(membership);
        }

        return new ModelAndView("redirect:/groups/" + id);
    }

    // Accept an invitation
    @PostMapping("/{id}/accept")
    public ModelAndView acceptInvite(@PathVariable Long id,
                                     @AuthenticationPrincipal OAuth2User principal) {
        User user = getCurrentUser(principal);
        GroupMember membership = groupMemberRepository
                .findByGroupIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Membership not found"));
        membership.setStatus("joined");
        groupMemberRepository.save(membership);

        return new ModelAndView("redirect:/groups/" + id);
    }

    @PostMapping("/{id}/delete")
    public ModelAndView delete(@PathVariable Long id,
                               @AuthenticationPrincipal OAuth2User principal) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User user = getCurrentUser(principal);

        GroupMember membership = groupMemberRepository
                .findByGroupIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Membership not found"));

        if (membership.getRole().equals("owner")) {
            groupRepository.deleteById(id);
        }

        return new ModelAndView("redirect:/groups");
    }

    // Group feed — shows all moments from the last newsletter week
    @GetMapping("/{id}/feed")
    public String groupFeed(@PathVariable Long id,
                            @RequestParam(defaultValue = "desc") String order,
                            @RequestParam(defaultValue = "grid") String layout,
                            @RequestParam(defaultValue = "none") String groupBy,
                            Model model,
                            @AuthenticationPrincipal OAuth2User principal) {

        User currentUser = getCurrentUser(principal);

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        boolean isMember = groupMemberRepository
                .findByGroupIdAndStatus(id, "joined")
                .stream()
                .anyMatch(gm -> gm.getUser().getId().equals(currentUser.getId()));

        if (!isMember) {
            return "redirect:/moments";
        }

        Optional<Weekly> latestWeekly = weeklyRepository
                .findFirstByGroupIdAndStatusOrderByWeekStartDesc(id, "sent");

        List<Moment> moments = List.of();
        LocalDateTime weekStart = null;
        LocalDateTime weekEnd = null;

        if (latestWeekly.isPresent()) {
            Weekly weekly = latestWeekly.get();
            weekStart = weekly.getWeekStart();
            weekEnd = weekly.getSendDate();

            List<Long> memberIds = groupMemberRepository
                    .findByGroupIdAndStatus(id, "joined")
                    .stream()
                    .map(gm -> gm.getUser().getId())
                    .collect(Collectors.toList());

            if (order.equals("asc")) {
                moments = momentRepository
                        .findByCreatedByIdInAndCreatedAtBetweenOrderByCreatedAtAsc(
                                memberIds, weekStart, weekEnd);
            } else {
                moments = momentRepository
                        .findByCreatedByIdInAndCreatedAtBetweenOrderByCreatedAtDesc(
                                memberIds, weekStart, weekEnd);
            }
        }

        // Build grouped map if needed
        if (groupBy.equals("user") && !moments.isEmpty()) {
            LinkedHashMap<User, List<Moment>> momentsByUser = new LinkedHashMap<>();
            for (Moment moment : moments) {
                momentsByUser
                        .computeIfAbsent(moment.getCreatedBy(), k -> new ArrayList<>())
                        .add(moment);
            }
            model.addAttribute("momentsByUser", momentsByUser);
        }

        model.addAttribute("group", group);
        model.addAttribute("moments", moments);
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("weekEnd", weekEnd);
        model.addAttribute("order", order);
        model.addAttribute("layout", layout);
        model.addAttribute("groupBy", groupBy);
        model.addAttribute("hasNewsletter", latestWeekly.isPresent());
        model.addAttribute("latestWeekly", latestWeekly.orElse(null));

        return "groups/feed";
    }

    private User getCurrentUser(OAuth2User principal) {
        String email = principal.getAttribute("email");
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}