package com.makers.memoir.repository;

import com.makers.memoir.model.Moment;
import org.springframework.data.repository.CrudRepository;

public interface MomentRepository extends CrudRepository<Moment, Long> {
}
