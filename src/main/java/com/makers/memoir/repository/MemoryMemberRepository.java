package com.makers.memoir.repository;

import com.makers.memoir.model.MemoryMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemoryMemberRepository extends JpaRepository<MemoryMember, Long> {
    List<MemoryMember> findByMemoryId(Long memoryId);
    Optional<MemoryMember> findByMemoryIdAndUserId(Long memoryId, Long userId);
    boolean existsByMemoryIdAndUserId(Long memoryId, Long userId);
}