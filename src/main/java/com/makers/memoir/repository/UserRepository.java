package com.makers.memoir.repository;

import com.makers.memoir.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
}
