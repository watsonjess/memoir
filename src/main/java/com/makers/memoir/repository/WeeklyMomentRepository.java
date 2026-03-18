package com.makers.memoir.repository;


import com.makers.memoir.model.Moment;
import com.makers.memoir.model.WeeklyMoment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WeeklyMomentRepository extends JpaRepository<WeeklyMoment, Long> {
    List<WeeklyMoment> findByWeeklyId(Long weeklyId);
    List<WeeklyMoment> findByWeeklyIdOrderByAddedAtAsc(Long weeklyId);
    List<WeeklyMoment> findByMomentId(Long momentId);
    boolean existsByWeeklyIdAndMomentId(Long weeklyId, Long momentId);

    @Query("SELECT wm.moment FROM WeeklyMoment wm WHERE wm.weekly.id = :weeklyId ORDER BY wm.addedAt ASC")
    List<Moment> findMomentsByWeeklyId(@Param("weeklyId") Long weeklyId);
}