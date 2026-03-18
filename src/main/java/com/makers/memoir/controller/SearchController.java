package com.makers.memoir.controller;

import com.makers.memoir.model.User;
import com.makers.memoir.repository.EventRepository;
import com.makers.memoir.repository.GroupRepository;
import com.makers.memoir.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private UserRepository userRepository;

    private String getEmailFromPrincipal(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken token) {
            return token.getPrincipal().getAttribute("email");
        }
        return principal.getName();
    }

    @GetMapping
    public String search(@RequestParam(name = "q", required = false, defaultValue = "") String query,
                         Model model,
                         Principal principal) {

        String trimmed = query.trim();

        if (!trimmed.isEmpty()) {
            User currentUser = userRepository.findByEmail(getEmailFromPrincipal(principal));

            List<User> users = userRepository.searchUsers(trimmed)
                    .stream()
                    .filter(u -> !u.getId().equals(currentUser.getId()))
                    .toList();

            model.addAttribute("users", users);
        }

        model.addAttribute("query", trimmed);
        return "search/results";
    }
}