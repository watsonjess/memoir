package com.makers.memoir.controller;

import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@Controller
public class MomentController {
	@RequestMapping(value = "/")
	public RedirectView index() {
		return new RedirectView("/moments");
	}
}

// Cloudinary upload code:
// if (!file.isEmpty()) {
//		Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
//		String publicUrl = (String) uploadResult.get("secure_url");
//            user.setFileUrl(publicUrl);
//        }