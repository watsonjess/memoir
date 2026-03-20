package com.makers.memoir.controller;

import com.makers.memoir.model.User;
import com.makers.memoir.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;


@ControllerAdvice
public class GlobalModelAdvice {

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute("loggedInUser")
    public User loggedInUser() {
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof DefaultOidcUser oidcUser) {
            String email = (String) oidcUser.getAttributes().get("email");
            return userRepository.findByEmail(email);
        }

        return null;
    }
}