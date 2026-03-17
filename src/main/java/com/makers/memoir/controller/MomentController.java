package com.makers.memoir.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class MomentController {
	@RequestMapping(value = "/")
	public RedirectView index() {
		return new RedirectView("/moments");
	}
}
