package com.makers.memoir.controller;

import com.makers.memoir.model.Friend;
import com.makers.memoir.model.User;
import com.makers.memoir.repository.FriendRepository;
import com.makers.memoir.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRepository friendRepository;

    @GetMapping("/profile")
    public String profile(@ModelAttribute("loggedInUser") User loggedInUser) {
        return "redirect:/profile/" + loggedInUser.getUsername();
    }

    @GetMapping("/profile/{username}")
    public String profileByUsername(@PathVariable String username, Model model,
                                    @ModelAttribute("loggedInUser") User loggedInUser) {
        User profileUser = userRepository.findByUsername(username).orElse(null);
        if (profileUser == null) return "redirect:/";

        // Friends list
        List<Friend> acceptedFriends = friendRepository
                .findByUserIdAndStatus(profileUser.getId(), "ACCEPTED");

        List<User> friends = acceptedFriends.stream()
                .map(f -> {
                    Long friendId = f.getId().getRequesterId().equals(profileUser.getId())
                            ? f.getId().getAddresseeId()
                            : f.getId().getRequesterId();
                    return userRepository.findById(friendId).orElse(null);
                })
                .filter(u -> u != null)
                .collect(Collectors.toList());

        // Only show if viewing their own profile
        boolean isOwnProfile = profileUser.getUsername().equals(loggedInUser.getUsername());

        if (isOwnProfile) {
            List<Friend> incomingRequests = friendRepository
                    .findByIdAddresseeIdAndStatus(profileUser.getId(), "PENDING");
            List<Friend> outgoingRequests = friendRepository
                    .findByIdRequesterIdAndStatus(profileUser.getId(), "PENDING");
            List<Friend> blockedFriendships = friendRepository
                    .findByIdRequesterIdAndStatus(profileUser.getId(), "BLOCKED");

            model.addAttribute("incomingRequests", incomingRequests);
            model.addAttribute("outgoingRequests", outgoingRequests);
            model.addAttribute("blockedFriendships", blockedFriendships);
        }

        model.addAttribute("profileUser", profileUser);
        model.addAttribute("friends", friends);
        model.addAttribute("isOwnProfile", isOwnProfile);

        return "profile";
    }
}