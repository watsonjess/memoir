package com.makers.memoir.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.makers.memoir.model.*;
import com.makers.memoir.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
		User currentUser = userRepository.findByEmail(getUsernameFromPrincipal(principal));
		boolean isEditable = moment.getCreatedBy().getId().equals(currentUser.getId())
				&& moment.getCreatedAt().toLocalDate().equals(LocalDate.now());

		model.addAttribute("isEditable", isEditable);
		model.addAttribute("moment", moment);
		return "moments/show";
	}

	@GetMapping
	public String listMoments(Model model, Principal principal) {
		User currentUser = userRepository.findByEmail(getUsernameFromPrincipal(principal));
		List<Moment> moments = momentRepository.findByCreatedByIdOrderByCreatedAtDesc(currentUser.getId());
		model.addAttribute("moments", moments);
		Set<Long> editableMomentIds = moments.stream()
				.filter(m -> m.getCreatedBy().getId().equals(currentUser.getId()))
				.filter(m -> m.getCreatedAt().toLocalDate().equals(LocalDate.now()))
				.map(Moment::getId)
				.collect(Collectors.toSet());

		model.addAttribute("editableMomentIds", editableMomentIds);
		return "moments/index";
	}

	@GetMapping("/{id}/edit")
	public ModelAndView editMoment(@PathVariable Long id,
								   Principal principal) {
		User user = userRepository.findByEmail(getUsernameFromPrincipal(principal));
		Moment moment = momentRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Moment not found"));

		if (!moment.getCreatedBy().getId().equals(user.getId())) {
			return new ModelAndView("redirect:/moments/" + id);
		}

		if (!moment.getCreatedAt().toLocalDate().equals(LocalDate.now())) {
			return new ModelAndView("redirect:/moments/" + id);
		}

		ModelAndView modelAndView = new ModelAndView("moments/edit");
		modelAndView.addObject("moment", moment);
		return modelAndView;
	}

	@PostMapping("/{id}/edit")
	public RedirectView updateMoment(@PathVariable Long id,
									 @RequestParam String content,
									 @RequestParam(required = false) String location,
									 @RequestParam(required = false) Double latitude,
									 @RequestParam(required = false) Double longitude,
									 @RequestParam(required = false) MultipartFile image,
									 Principal principal) {
		User user = userRepository.findByEmail(getUsernameFromPrincipal(principal));
		Moment moment = momentRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Moment not found"));

		if (!moment.getCreatedBy().getId().equals(user.getId())) {
			return new RedirectView("/");
		}
		if (!moment.getCreatedAt().toLocalDate().equals(LocalDate.now())) {
			return new RedirectView("/");
		}

		moment.setContent(content);
		moment.setLocation(location);
		moment.setLatitude(latitude);
		moment.setLongitude(longitude);

		if (image != null && !image.isEmpty()) {
			try {
				Map uploadResult = cloudinary.uploader().upload(image.getBytes(),
						ObjectUtils.emptyMap());
				moment.setImageUrl((String) uploadResult.get("secure_url"));
			} catch (IOException e) {
				throw new RuntimeException("Image upload failed", e);
			}
		}

		momentRepository.save(moment);
		return new RedirectView("/moments");
	}

	@RequestMapping(value = "/")
	public RedirectView home() {
		return new RedirectView("/moments");
	}
}