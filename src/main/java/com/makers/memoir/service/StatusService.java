package com.makers.memoir.service;

import com.makers.memoir.model.GroupMember;
import com.makers.memoir.model.User;
import com.makers.memoir.repository.GroupMemberRepository;
import com.makers.memoir.repository.MomentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class StatusService {
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private MomentRepository momentRepository;

    public int calculateAllTimeMomentPosts(User user){
        List<GroupMember> memberships = groupMemberRepository.findByUserId(user.getId());
        if (memberships.isEmpty()) return 100;

        long totalExpectedDays = 0;
        long totalDaysPosted = 0;

        for (GroupMember member : memberships) {
            LocalDateTime joinedAt = member.getJoinedAt();
            long daysInGroup = ChronoUnit.DAYS.between(joinedAt.toLocalDate(), LocalDate.now()) + 1;

            totalExpectedDays += daysInGroup;

            totalDaysPosted += momentRepository.countUniqueDaysPosted(
                    user.getId(),
                    member.getGroup().getId(),
                    joinedAt
            );
        }

        if (totalExpectedDays == 0) return 100;
        return (int) (((double) totalDaysPosted / totalExpectedDays) * 100);
    }
    }



