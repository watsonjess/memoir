package com.makers.memoir.repository;

import com.makers.memoir.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    public Optional<User> findUserByEmail(String email);
    public User findByEmail(String email);

    public Optional<User> findByUsername(String username);

    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(u.firstname) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(u.lastname) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY u.username ASC
            """)
    List<User> searchUsers(@Param("query") String query);
}
