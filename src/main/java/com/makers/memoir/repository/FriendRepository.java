package com.makers.memoir.repository;

import com.makers.memoir.Model.Friend;
import com.makers.memoir.Model.FriendId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, FriendId> {

    @Query("SELECT f FROM Friend f WHERE (f.id.requesterId = :userId OR f.id.addresseeId = :userId) AND f.status = :status")
    List<Friend> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    List<Friend> findByIdAddresseeIdAndStatus(Long addresseeId, String status);

    List<Friend> findByIdRequesterIdAndStatus(Long requesterId, String status);

    Optional<Friend> findByIdRequesterIdAndIdAddresseeId(Long requesterId, Long addresseeId);

    boolean existsByIdRequesterIdAndIdAddresseeIdAndStatus(Long requesterId, Long addresseeId, String status);
}