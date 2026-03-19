package com.makers.memoir.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.makers.memoir.model.Group;
import com.makers.memoir.model.GroupMember;
import com.makers.memoir.model.Moment;
import com.makers.memoir.model.User;
import com.makers.memoir.repository.GroupMemberRepository;
import com.makers.memoir.repository.GroupRepository;
import com.makers.memoir.repository.MomentRepository;
import com.makers.memoir.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/moments")
public class MomentController {

	@Autowired
	MomentRepository momentRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	GroupMemberRepository groupMemberRepository;

	@Autowired
	GroupRepository groupRepository;

	@Autowired
	Cloudinary cloudinary;

	private String getUsernameFromPrincipal(Principal principal) {
		if (principal instanceof OAuth2AuthenticationToken) {
			OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) principal;
			return token.getPrincipal().getAttribute("email");
		}
		return principal.getName();
	}

	@GetMapping("/new")
	public String newMomentForm(Model model, Principal principal) {
		User currentUser = userRepository.findByEmail(getUsernameFromPrincipal(principal));

		List<GroupMember> memberships = groupMemberRepository
				.findByUserIdAndStatus(currentUser.getId(), "joined");

		model.addAttribute("groups", memberships.stream()
				.map(GroupMember::getGroup)
				.collect(Collectors.toList()));

		return "moments/new";
	}

	@PostMapping("/new")
	public RedirectView createMoment(@RequestParam("image") MultipartFile image,
									 @RequestParam("content") String content,
									 @RequestParam("location") String location,
									 @RequestParam("groupId") Long groupId,
									 Principal principal) throws Exception {
		User currentUser = userRepository.findByEmail(getUsernameFromPrincipal(principal));

		// Upload image to Cloudinary
		Map uploadResult = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap());
		String imageUrl = (String) uploadResult.get("secure_url");

		// Fetch the group
		Group group = groupRepository.findById(groupId).orElseThrow();

		// Build and save moment
		Moment moment = new Moment();
		moment.setCreatedBy(currentUser);
		moment.setImageUrl(imageUrl);
		moment.setContent(content);
		moment.setLocation(location.isEmpty() ? null : location);
		moment.getGroups().add(group);
		momentRepository.save(moment);

		return new RedirectView("/moments/" + moment.getId());
	}

	@GetMapping("/{id}")
	public String viewMoment(@PathVariable Long id, Model model, Principal principal) {
		Moment moment = momentRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Moment not found"));

		model.addAttribute("moment", moment);
		return "moments/show";
	}

	@GetMapping
	public String listMoments(Model model, Principal principal) {
		User currentUser = userRepository.findByEmail(getUsernameFromPrincipal(principal));
		List<Moment> moments = momentRepository.findByCreatedByIdOrderByCreatedAtDesc(currentUser.getId());
		model.addAttribute("moments", moments);
		return "moments/index";
	}

	@RequestMapping(value = "/")
	public RedirectView home() {
		return new RedirectView("/moments");
	}
}