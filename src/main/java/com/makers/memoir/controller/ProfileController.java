package com.makers.memoir.controller;

import com.makers.memoir.model.User;
import com.makers.memoir.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/profile")
    public String profile(@ModelAttribute("loggedInUser") User loggedInUser) {
        return "redirect:/profile/" + loggedInUser.getUsername();
    }

    @GetMapping("/profile/{username}")
    public String profileByUsername(@PathVariable String username, Model model) {
        User profileUser = userRepository.findByUsername(username).orElse(null);
        model.addAttribute("profileUser", profileUser);
        return "profile";
    }
}