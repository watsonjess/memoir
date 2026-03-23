package com.makers.memoir.repository;

import com.makers.memoir.model.Memory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemoryRepository extends JpaRepository<Memory, Long> {

    @Query("SELECT m FROM Memory m JOIN m.members mm WHERE mm.user.id = :userId")
    List<Memory> findByMemberId(@Param("userId") Long userId);
}