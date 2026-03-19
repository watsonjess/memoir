package com.makers.memoir.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/archive")
public class ArchiveController {
    @GetMapping
    public ModelAndView archive(){
        return new ModelAndView("archive/index");
    }
}
