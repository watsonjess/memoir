package com.makers.memoir.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.makers.memoir.model.*;
import com.makers.memoir.repository.*;
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
	EventRepository eventRepository;

	@Autowired
	EventMomentRepository eventMomentRepository;

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
	public String newMomentForm(Model model, Principal principal,@RequestParam(required = false) Long groupId,
								@RequestParam(required = false) Long eventId) {
		User currentUser = userRepository.findByEmail(getUsernameFromPrincipal(principal));

		List<GroupMember> memberships = groupMemberRepository
				.findByUserIdAndStatus(currentUser.getId(), "joined");

		model.addAttribute("groups", memberships.stream()
				.map(GroupMember::getGroup)
				.collect(Collectors.toList()));

		List<Group> groups = memberships.stream()
				.map(GroupMember::getGroup)
				.collect(Collectors.toList());

		Map<Long, Long> groupEventMap = groups.stream()
				.filter(g -> g.getType().equals("event"))
				.collect(Collectors.toMap(
						Group::getId,
						g -> eventRepository.findByGroupIdOrderByStartDateDesc(g.getId())
								.stream().findFirst()
								.map(Event::getId)
								.orElse(null),
						(a, b) -> a
				));

		model.addAttribute("groups", groups);
		model.addAttribute("groupEventMap", groupEventMap);
		model.addAttribute("eventId", eventId);
		model.addAttribute("preselectedGroupId", groupId);

		return "moments/new";
	}

	@PostMapping("/new")
	public RedirectView createMoment(@RequestParam("image") MultipartFile image,
									 @RequestParam("content") String content,
									 @RequestParam("location") String location,
									 @RequestParam("groupId") Long groupId,
									 @RequestParam(value = "eventId", required = false) Long eventId,
									 @RequestParam(value = "latitude",  required = false) Double latitude,
									 @RequestParam(value = "longitude", required = false) Double longitude,
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
		moment.setLatitude(latitude);
		moment.setLongitude(longitude);
		momentRepository.save(moment);

		if (eventId != null) {
			Event event = eventRepository.findById(eventId).orElseThrow();
			EventMoment em = new EventMoment();
			em.setEvent(event);
			em.setMoment(moment);
			eventMomentRepository.save(em);
			return new RedirectView("/groups/" + event.getGroup().getId() + "/events/" + eventId);
		}

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