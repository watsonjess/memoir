package com.makers.memoir.controller;

import com.makers.memoir.model.User;
import com.makers.memoir.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private User getCurrentUser(Principal principal) {
        return userRepository.findByEmail(getEmailFromPrincipal(principal));
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

    @GetMapping(produces = "application/json")
    @ResponseBody
    public Map<String, Object> searchJson(
            @RequestParam(name = "q", required = false, defaultValue = "") String query,
            Principal principal) {

        String q = query.trim();
        Map<String, Object> result = new HashMap<>();

        if (q.isEmpty()) {
            result.put("users",  List.of());
            return result;
        }

        User me = getCurrentUser(principal);

        List<Map<String, Object>> users = userRepository.searchUsers(q).stream()
                .filter(u -> !u.getId().equals(me.getId()))
                .limit(3)
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id",           u.getId());
                    m.put("username",     u.getUsername());
                    m.put("firstname",    u.getFirstname());
                    m.put("lastname",     u.getLastname());
                    m.put("profileImage", u.getProfileImage());
                    return m;
                })
                .collect(Collectors.toList());

        result.put("users",  users);
        return result;
    }
}