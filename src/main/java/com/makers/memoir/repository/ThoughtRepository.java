package com.makers.memoir.repository;

import com.makers.memoir.model.Thought;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThoughtRepository extends JpaRepository<Thought, Long> {
    List<Thought> findByMemoryIdOrderByCreatedAtDesc(Long memoryId);
}