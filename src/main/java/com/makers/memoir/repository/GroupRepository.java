package com.makers.memoir.repository;

import com.makers.memoir.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByCreatedById(Long userId);
}