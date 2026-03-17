package com.makers.memoir.controller;

import com.makers.memoir.model.Moment;
import com.makers.memoir.model.Weekly;
import com.makers.memoir.repository.GroupRepository;
import com.makers.memoir.repository.WeeklyMomentRepository;
import com.makers.memoir.repository.WeeklyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/groups/{groupId}/weeklies")
public class WeeklyController {

    @Autowired
    private WeeklyRepository weeklyRepository;

    @Autowired
    private WeeklyMomentRepository weeklyMomentRepository;

    @Autowired
    private GroupRepository groupRepository;

    // List all weeklies (archive) for a group
    @GetMapping
    public ModelAndView index(@PathVariable Long groupId) {
        List<Weekly> weeklies = weeklyRepository.findByGroupIdOrderByWeekStartDesc(groupId);

        ModelAndView modelAndView = new ModelAndView("weeklies/index");
        modelAndView.addObject("weeklies", weeklies);
        modelAndView.addObject("groupId", groupId);
        return modelAndView;
    }

    // Show a single weekly digest and its moments
    @GetMapping("/{id}")
    public ModelAndView show(@PathVariable Long groupId,
                             @PathVariable Long id) {
        Weekly weekly = weeklyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Weekly not found"));
        List<Moment> moments = weeklyMomentRepository.findMomentsByWeeklyId(id);

        ModelAndView modelAndView = new ModelAndView("weeklies/show");
        modelAndView.addObject("weekly", weekly);
        modelAndView.addObject("moments", moments);
        return modelAndView;
    }

    // Show the currently open digest for a group
    @GetMapping("/current")
    public ModelAndView current(@PathVariable Long groupId) {
        Weekly weekly = weeklyRepository.findFirstByGroupIdAndStatusOrderByWeekStartDesc(groupId, "open")
                .orElseThrow(() -> new RuntimeException("No open digest found"));
        List<Moment> moments = weeklyMomentRepository.findMomentsByWeeklyId(weekly.getId());

        ModelAndView modelAndView = new ModelAndView("weeklies/show");
        modelAndView.addObject("weekly", weekly);
        modelAndView.addObject("moments", moments);
        return modelAndView;
    }
}