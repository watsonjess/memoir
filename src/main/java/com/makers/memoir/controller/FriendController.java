package com.makers.memoir.controller;

import com.makers.memoir.model.Friend;
import com.makers.memoir.model.User;
import com.makers.memoir.repository.FriendRepository;
import com.makers.memoir.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/friends")
public class FriendController {

    @Autowired
    FriendRepository friendRepository;

    @Autowired
    UserRepository userRepository;

    private String getUsernameFromPrincipal(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) principal;
            return token.getPrincipal().getAttribute("email");
        }
        return principal.getName();
    }

    @GetMapping
    public String friendsPage(Model model, Principal principal) {
        User currentUser = userRepository.findByEmail(getUsernameFromPrincipal(principal));

        List<Friend> acceptedFriendships = friendRepository
                .findByUserIdAndStatus(currentUser.getId(), "ACCEPTED");

        List<User> friends = acceptedFriendships.stream()
                .map(f -> {
                    Long friendId = f.getId().getRequesterId().equals(currentUser.getId())
                            ? f.getId().getAddresseeId()
                            : f.getId().getRequesterId();
                    return userRepository.findById(friendId).orElse(null);
                })
                .filter(u -> u != null)
                .collect(Collectors.toList());

        List<Friend> incomingRequests = friendRepository
                .findByIdAddresseeIdAndStatus(currentUser.getId(), "PENDING");

        List<Friend> outgoingRequests = friendRepository
                .findByIdRequesterIdAndStatus(currentUser.getId(), "PENDING");

        List<Friend> blockedFriendships = friendRepository
                .findByIdRequesterIdAndStatus(currentUser.getId(), "BLOCKED");

        model.addAttribute("friends", friends);
        model.addAttribute("incomingRequests", incomingRequests);
        model.addAttribute("outgoingRequests", outgoingRequests);
        model.addAttribute("blockedFriendships", blockedFriendships);

        return "friends/index";
    }

    @PostMapping("/request/{username}")
    public RedirectView sendRequest(@PathVariable String username, Principal principal) {
        User requester = userRepository.findByEmail(getUsernameFromPrincipal(principal));
        User addressee = userRepository.findByUsername(username).orElseThrow();

        boolean blockedByAddressee = friendRepository
                .existsByIdRequesterIdAndIdAddresseeIdAndStatus(addressee.getId(), requester.getId(), "BLOCKED");
        boolean blockedByRequester = friendRepository
                .existsByIdRequesterIdAndIdAddresseeIdAndStatus(requester.getId(), addressee.getId(), "BLOCKED");

        if (blockedByAddressee || blockedByRequester) {
            return new RedirectView("/profile/" + username);
        }

        Friend friendship = new Friend(requester.getId(), addressee.getId());
        friendship.setRequester(requester);
        friendship.setAddressee(addressee);
        friendRepository.save(friendship);

        return new RedirectView("/profile/" + username);
    }

    @PostMapping("/accept/{username}")
    public RedirectView acceptRequest(@PathVariable String username, Principal principal) {
        User addressee = userRepository.findByEmail(getUsernameFromPrincipal(principal));
        User requester = userRepository.findByUsername(username).orElseThrow();

        Optional<Friend> friendship = friendRepository
                .findByIdRequesterIdAndIdAddresseeId(requester.getId(), addressee.getId());

        friendship.ifPresent(f -> {
            f.setStatus("ACCEPTED");
            friendRepository.save(f);
        });

        return new RedirectView("/profile/" + addressee.getUsername());
    }

@PostMapping("/decline/{username}")
public RedirectView declineRequest(@PathVariable String username, Principal principal) {
    User addressee = userRepository.findByEmail(getUsernameFromPrincipal(principal));
    User requester = userRepository.findByUsername(username).orElseThrow();

    Optional<Friend> friendship = friendRepository
            .findByIdRequesterIdAndIdAddresseeId(requester.getId(), addressee.getId());

    friendship.ifPresent(f -> friendRepository.delete(f));

    return new RedirectView("/profile/" + addressee.getUsername());
}

    @PostMapping("/block/{username}")
    public RedirectView blockUser(@PathVariable String username, Principal principal) {
        User requester = userRepository.findByEmail(getUsernameFromPrincipal(principal));
        User addressee = userRepository.findByUsername(username).orElseThrow();

        Optional<Friend> existing = friendRepository
                .findByIdRequesterIdAndIdAddresseeId(requester.getId(), addressee.getId());

        // Also check reverse direction - fix from old project
        if (existing.isEmpty()) {
            existing = friendRepository
                    .findByIdRequesterIdAndIdAddresseeId(addressee.getId(), requester.getId());
        }

        if (existing.isPresent()) {
            existing.get().setStatus("BLOCKED");
            friendRepository.save(existing.get());
        } else {
            Friend friendship = new Friend(requester.getId(), addressee.getId());
            friendship.setRequester(requester);
            friendship.setAddressee(addressee);
            friendship.setStatus("BLOCKED");
            friendRepository.save(friendship);
        }

        return new RedirectView("/profile/" + username);
    }

    @PostMapping("/unfriend/{username}")
    public RedirectView unfriend(@PathVariable String username, Principal principal) {
        User currentUser = userRepository.findByEmail(getUsernameFromPrincipal(principal));
        User other = userRepository.findByUsername(username).orElseThrow();

        Optional<Friend> friendship = friendRepository
                .findByIdRequesterIdAndIdAddresseeId(currentUser.getId(), other.getId());

        if (friendship.isEmpty()) {
            friendship = friendRepository
                    .findByIdRequesterIdAndIdAddresseeId(other.getId(), currentUser.getId());
        }

        friendship.ifPresent(f -> friendRepository.delete(f));

        return new RedirectView("/profile/" + username);
    }
}