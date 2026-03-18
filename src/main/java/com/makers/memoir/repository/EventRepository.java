package com.makers.memoir.repository;

import com.makers.memoir.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByGroupId(Long groupId);
    List<Event> findByGroupIdOrderByStartDateDesc(Long groupId);
    List<Event> findByCreatedById(Long userId);
}