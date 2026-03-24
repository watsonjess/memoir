package com.makers.memoir.repository;

import com.makers.memoir.model.Moment;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

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

    Page<Moment> findByCreatedByIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    @Query(value = "SELECT COUNT(DISTINCT m.created_at::date) FROM moments m " +
            "JOIN moment_groups mg ON m.id = mg.moment_id " +
            "WHERE m.created_by = :userId " +
            "AND mg.group_id = :groupId " +
            "AND m.created_at >= :joinDate",
            nativeQuery = true)
    long countUniqueDaysPosted(@Param("userId") Long userId,
                               @Param("groupId") Long groupId,
                               @Param("joinDate") LocalDateTime joinDate);
}
