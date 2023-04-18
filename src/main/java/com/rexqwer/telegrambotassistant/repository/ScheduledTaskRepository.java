package com.rexqwer.telegrambotassistant.repository;

import com.rexqwer.telegrambotassistant.domain.ScheduledTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data PostgreSQL репозиторий для {@link ScheduledTask}.
 */
@Repository
public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {

    List<ScheduledTask> findAllByNextStartTimeIsLessThanEqualAndActiveTrue(LocalDateTime currentDate);

    @Query(value = "SELECT scheduled_task.id FROM scheduled_task WHERE scheduled_task.active = true AND scheduled_task.user_id = :userId", nativeQuery = true)
    List<Long> selectActiveTaskIdsForUser(@Param("userId") Long userId);

}
