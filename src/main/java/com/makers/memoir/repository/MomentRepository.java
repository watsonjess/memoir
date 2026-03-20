package com.makers.memoir.repository;

import com.makers.memoir.model.Moment;
import org.springframework.data.repository.CrudRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface MomentRepository extends CrudRepository<Moment, Long> {
    List<Moment> findByCreatedByIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
    List<Moment> findByCreatedById(Long userId);
    List<Moment> findByCreatedByIdOrderByCreatedAtDesc(Long userId);
    List<Moment> findByCreatedByIdInAndCreatedAtBetweenOrderByCreatedAtAsc(
            List<Long> userIds, LocalDateTime start, LocalDateTime end);

    List<Moment> findByCreatedByIdInAndCreatedAtBetweenOrderByCreatedAtDesc(
            List<Long> userIds, LocalDateTime start, LocalDateTime end);
}
