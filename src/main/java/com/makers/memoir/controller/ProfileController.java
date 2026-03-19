package com.makers.memoir.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

@GetMapping("/profile")
public String profile(Model model, @AuthenticationPrincipal OidcUser principal) {
    return "profile";
}
}