package com.makers.memoir.repository;

import com.makers.memoir.model.EventMoment;
import com.makers.memoir.model.Moment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventMomentRepository extends JpaRepository<EventMoment, Long> {
    List<EventMoment> findByEventId(Long eventId);
    List<EventMoment> findByEventIdOrderByAddedAtAsc(Long eventId);
    List<EventMoment> findByMomentId(Long momentId);
    boolean existsByEventIdAndMomentId(Long eventId, Long momentId);

    @Query("SELECT em.moment FROM EventMoment em WHERE em.event.id = :eventId ORDER BY em.addedAt ASC")
    List<Moment> findMomentsByEventId(@Param("eventId") Long eventId);
}