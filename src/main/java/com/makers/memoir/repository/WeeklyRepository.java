package com.makers.memoir.repository;

import com.makers.memoir.model.Weekly;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WeeklyRepository extends JpaRepository<Weekly, Long> {
    List<Weekly> findByGroupId(Long groupId);
    List<Weekly> findByGroupIdOrderByWeekStartDesc(Long groupId);
    Optional<Weekly> findFirstByGroupIdAndStatusOrderByWeekStartDesc(Long groupId, String status);
    List<Weekly> findByStatus(String status);
}