package com.rexqwer.telegrambotassistant.repository;

import com.rexqwer.telegrambotassistant.domain.reference.ScheduledTaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data PostgreSQL репозиторий для {@link ScheduledTaskType}.
 */
@Repository
public interface ScheduledTaskTypeRepository extends JpaRepository<ScheduledTaskType, Long> {

    ScheduledTaskType findByCode(Integer code);
}