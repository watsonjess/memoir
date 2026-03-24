package com.makers.memoir.repository;

import com.makers.memoir.model.Group;
import com.makers.memoir.model.Weekly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WeeklyRepository extends JpaRepository<Weekly, Long> {
    List<Weekly> findByGroupId(Long groupId);
    List<Weekly> findByGroupIdOrderByWeekStartDesc(Long groupId);
    Optional<Weekly> findFirstByGroupIdAndStatusOrderByWeekStartDesc(Long groupId, String status);
    List<Weekly> findByStatus(String status);
    List<Weekly> findByGroupIdAndStatus(Long groupId, String status);
    Optional<Weekly> findByGroupAndWeekStart(Group group, LocalDateTime weekStart);
    List<Weekly> findByGroupIdInAndStatus(List<Long> groupIds, String status);

    List<Weekly> findByStatusAndSendDateBefore(String status, LocalDateTime dateTime);

    @Query("SELECT w FROM Weekly w WHERE w.group.id IN :groupIds AND w.sentAt >= :dayStart AND w.sentAt < :dayEnd")
    List<Weekly> findByGroupIdsAndSentDate(
            @Param("groupIds") List<Long> groupIds,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd
    );

}