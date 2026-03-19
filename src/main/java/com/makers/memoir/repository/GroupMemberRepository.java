package com.makers.memoir.repository;

import com.makers.memoir.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupId(Long groupId);
    List<GroupMember> findByUserId(Long userId);
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
    List<GroupMember> findByGroupIdAndStatus(Long groupId, String status);
    List<GroupMember> findByUserIdAndStatus(Long userId, String status);
}