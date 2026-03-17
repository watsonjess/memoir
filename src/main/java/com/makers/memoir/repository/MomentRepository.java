package com.makers.memoir.repository;

import com.makers.memoir.model.Moment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MomentRepository extends CrudRepository<Moment, Long> {
    List<Moment> findByCreatedById(Long userId);
    List<Moment> findByCreatedByIdOrderByCreatedAtDesc(Long userId);
}
