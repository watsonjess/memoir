package com.makers.memoir.controller;

import com.makers.memoir.model.Group;
import com.makers.memoir.model.GroupMember;
import com.makers.memoir.model.User;
import com.makers.memoir.repository.GroupMemberRepository;
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
@RequestMapping("/groups")
public class GroupController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private UserRepository userRepository;

    // List all groups the current user belongs to
    @GetMapping
    public ModelAndView index(@AuthenticationPrincipal OAuth2User principal) {
        /*User user = getCurrentUser(principal);
        List<GroupMember> memberships = groupMemberRepository.findByUserId(user.getId());

        ModelAndView modelAndView = new ModelAndView("groups/index");
        modelAndView.addObject("pageTitle", "Groups");
        modelAndView.addObject("memberships", memberships);*/
        ModelAndView modelAndView = new ModelAndView("groups/index");
        return modelAndView;
    }

    // Show a single group
    @GetMapping("/{id}")
    public ModelAndView show(@PathVariable Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        List<GroupMember> members = groupMemberRepository.findByGroupIdAndStatus(id, "joined");

        ModelAndView modelAndView = new ModelAndView("groups/show");
        modelAndView.addObject("group", group);
        modelAndView.addObject("members", members);
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
                               @ModelAttribute Group group) {
        User user = getCurrentUser(principal);
        group.setCreatedBy(user);
        groupRepository.save(group);

        // Add creator as owner member with status joined
        GroupMember ownerMembership = new GroupMember(group, user, "owner");
        ownerMembership.setStatus("joined");
        groupMemberRepository.save(ownerMembership);

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

    private User getCurrentUser(OAuth2User principal) {
        String email = principal.getAttribute("email");
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}