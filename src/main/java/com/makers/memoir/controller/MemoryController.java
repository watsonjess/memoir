package com.makers.memoir.controller;

import com.cloudinary.Cloudinary;
import com.makers.memoir.model.Memory;
import com.makers.memoir.model.MemoryMember;
import com.makers.memoir.model.Thought;
import com.makers.memoir.model.User;
import com.makers.memoir.repository.MemoryMemberRepository;
import com.makers.memoir.repository.MemoryRepository;
import com.makers.memoir.repository.ThoughtRepository;
import com.makers.memoir.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//GET /memories Pinboard view all your memories
// GET /memories/new Create a new memory
// POST /memories Save new memory
// GET /memories/{id} Expanded view all thoughts for a memory
// POST /memories/{id}/thoughts Add a thought
// POST /memories/{id}/invite Invite a user
// POST /memories/{id}/position Save drag position

@Controller
@RequestMapping("/memories")
public class MemoryController {

    @Autowired
    MemoryRepository memoryRepository;

    @Autowired
    MemoryMemberRepository memoryMemberRepository;

    @Autowired
    ThoughtRepository thoughtRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    Cloudinary cloudinary;

    private User getCurrentUser(OAuth2User principal) {
        String email = principal.getAttribute("email");
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Pinboard: all memories you are a member of
    @GetMapping
    public String index(Model model, @AuthenticationPrincipal OAuth2User principal) {
        User currentUser = getCurrentUser(principal);
        List<Memory> memories = memoryRepository.findByMemberId(currentUser.getId());
        model.addAttribute("memories", memories);
        return "memories/index";
    }

    // Show create memory form
    @GetMapping("/new")
    public String newMemory(Model model) {
        model.addAttribute("memory", new Memory());
        return "memories/new";
    }

    // Create a new memory
    @PostMapping
    public String create(@RequestParam("name") String name,
                         @RequestParam(value = "description", required = false) String description,
                         @RequestParam(value = "coverImage", required = false) MultipartFile coverImage,
                         @AuthenticationPrincipal OAuth2User principal) throws Exception {
        User currentUser = getCurrentUser(principal);

        Memory memory = new Memory();
        memory.setName(name);
        memory.setDescription(description);
        memory.setCreatedBy(currentUser);

        // Upload cover image if provided
        if (coverImage != null && !coverImage.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(
                    coverImage.getBytes(), new HashMap<>());
            memory.setCoverImageUrl((String) uploadResult.get("secure_url"));
        }

        memoryRepository.save(memory);

        // Add creator as owner member
        MemoryMember ownerMembership = new MemoryMember(memory, currentUser, "owner");
        memoryMemberRepository.save(ownerMembership);

        return "redirect:/memories";
    }

    // View a single memory and its thoughts
    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model,
                       @AuthenticationPrincipal OAuth2User principal) {
        User currentUser = getCurrentUser(principal);
        Memory memory = memoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Memory not found"));

        // Check user is a member
        boolean isMember = memoryMemberRepository
                .existsByMemoryIdAndUserId(id, currentUser.getId());
        if (!isMember) {
            return "redirect:/memories";
        }

        List<Thought> thoughts = thoughtRepository.findByMemoryIdOrderByCreatedAtDesc(id);
        List<MemoryMember> members = memoryMemberRepository.findByMemoryId(id);

        MemoryMember currentMembership = memoryMemberRepository
                .findByMemoryIdAndUserId(id, currentUser.getId())
                .orElse(null);

        model.addAttribute("memory", memory);
        model.addAttribute("thoughts", thoughts);
        model.addAttribute("members", members);
        model.addAttribute("currentMembership", currentMembership);
        model.addAttribute("isOwner", currentMembership != null
                && currentMembership.getRole().equals("owner"));

        return "memories/show";
    }

    // Add a thought to a memory
    @PostMapping("/{id}/thoughts")
    public String addThought(@PathVariable Long id,
                             @RequestParam(value = "content", required = false) String content,
                             @RequestParam(value = "image", required = false) MultipartFile image,
                             @AuthenticationPrincipal OAuth2User principal) throws Exception {
        User currentUser = getCurrentUser(principal);

        // Check user is a member
        boolean isMember = memoryMemberRepository
                .existsByMemoryIdAndUserId(id, currentUser.getId());
        if (!isMember) {
            return "redirect:/memories";
        }

        Memory memory = memoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Memory not found"));

        Thought thought = new Thought();
        thought.setMemory(memory);
        thought.setCreatedBy(currentUser);
        thought.setContent(content);

        if (image != null && !image.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(
                    image.getBytes(), new HashMap<>());
            thought.setImageUrl((String) uploadResult.get("secure_url"));
        }

        thoughtRepository.save(thought);

        return "redirect:/memories/" + id;
    }

    // Invite a user to a memory by username
    @PostMapping("/{id}/invite")
    public String invite(@PathVariable Long id,
                         @RequestParam("username") String username,
                         @AuthenticationPrincipal OAuth2User principal) {
        User currentUser = getCurrentUser(principal);

        // Only owners can invite
        MemoryMember membership = memoryMemberRepository
                .findByMemoryIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Not a member"));

        if (!membership.getRole().equals("owner")) {
            return "redirect:/memories/" + id;
        }

        Memory memory = memoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Memory not found"));

        User invitee = userRepository.findByUsername(username)
                .orElse(null);

        if (invitee != null && !memoryMemberRepository
                .existsByMemoryIdAndUserId(id, invitee.getId())) {
            MemoryMember newMember = new MemoryMember(memory, invitee, "contributor");
            memoryMemberRepository.save(newMember);
        }

        return "redirect:/memories/" + id;
    }

    // Save drag position which is then called via JavaScript when a polaroid is dropped
    @PostMapping("/{id}/position")
    @ResponseBody
    public ResponseEntity<Void> updatePosition(@PathVariable Long id,
                                               @RequestParam Double x,
                                               @RequestParam Double y,
                                               @AuthenticationPrincipal OAuth2User principal) {
        User currentUser = getCurrentUser(principal);

        boolean isMember = memoryMemberRepository
                .existsByMemoryIdAndUserId(id, currentUser.getId());
        if (!isMember) {
            return ResponseEntity.status(403).build();
        }

        Memory memory = memoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Memory not found"));

        memory.setPinX(x);
        memory.setPinY(y);
        memoryRepository.save(memory);

        return ResponseEntity.ok().build();
    }

    // Delete a memory (owner only)
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal OAuth2User principal) {
        User currentUser = getCurrentUser(principal);

        MemoryMember membership = memoryMemberRepository
                .findByMemoryIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Not a member"));

        if (membership.getRole().equals("owner")) {
            memoryRepository.deleteById(id);
        }

        return "redirect:/memories";
    }
}