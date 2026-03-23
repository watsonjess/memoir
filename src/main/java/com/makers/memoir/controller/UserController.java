package com.makers.memoir.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.makers.memoir.model.User;
import com.makers.memoir.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

@Controller
public class UserController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    private Cloudinary cloudinary;

    @GetMapping("/after-login")
    public RedirectView afterLogin(HttpServletRequest request,
                                   HttpServletResponse response) {
        DefaultOidcUser principal = (DefaultOidcUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String email = (String) principal.getAttributes().get("email");

        // check if user exists in database, if not, add them
        User currentUser = userRepository
                .findUserByEmail(email)
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(email);
                    String nickname = (String) principal.getAttributes().get("nickname");
                    user.setUsername(nickname);
                    return userRepository.save(user);
                });
        // if the user doesn't have their information filled out, take to setup page
        if (currentUser.getFirstname() == null || currentUser.getFirstname().isEmpty()) {
            return new RedirectView("/setup");
        }

        // save current page they are on
        SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);

        if (savedRequest != null) {
            String targetUrl = savedRequest.getRedirectUrl();

            // if they come from home ( via login button), send to profile
            // if they come from any other page, return them to the page they were on
            URI uri = URI.create(targetUrl);

            if (uri.getPath().equals("/")) {
                return new RedirectView("/");
            }

            return new RedirectView(targetUrl);
        }

        return new RedirectView("/");
    }

    @GetMapping("/setup")
    public ModelAndView setup() {
        DefaultOidcUser principal = (DefaultOidcUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        String email = (String) principal.getAttributes().get("email");
        User user = userRepository.findByEmail(email);

        ModelAndView setupPage = new ModelAndView("setup");
        setupPage.addObject("isFirstSetup", true);
        setupPage.addObject("user", user);
        return setupPage;
    }

    @PostMapping("/setup")
    public RedirectView saveProfile(@RequestParam("profile-picture") MultipartFile profilePicture,
                                    @RequestParam String firstName,
                                    @RequestParam String lastName,
                                    @RequestParam String username,
                                    @RequestParam boolean isFirstSetup) throws IOException {
        DefaultOidcUser principal = (DefaultOidcUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String email = (String) principal.getAttributes().get("email");
        User user = userRepository.findByEmail(email);

        user.setFirstname(firstName);
        user.setLastname(lastName);
        user.setUsername(username);

        if (!profilePicture.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(profilePicture.getBytes(), ObjectUtils.emptyMap());
            String publicUrl = (String) uploadResult.get("secure_url");
            user.setProfileImage(publicUrl);
        }

        userRepository.save(user);
        if (isFirstSetup) {
            return new RedirectView("/");
        } else {
            return new RedirectView("/profile/" + user.getUsername());
        }
    }

    @GetMapping("/profile/edit")
    public ModelAndView editProfilePage() {
        DefaultOidcUser principal = (DefaultOidcUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String email = (String) principal.getAttributes().get("email");
        User user = userRepository.findByEmail(email);

        ModelAndView editProfile = new ModelAndView("setup");
        editProfile.addObject("isFirstSetup", false);
        editProfile.addObject("user", user);
        return editProfile;
    }
}
