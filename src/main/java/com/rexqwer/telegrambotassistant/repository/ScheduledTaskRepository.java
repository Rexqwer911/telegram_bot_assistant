package com.rexqwer.telegrambotassistant.repository;

import com.rexqwer.telegrambotassistant.domain.ScheduledTask;
import com.rexqwer.telegrambotassistant.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data PostgreSQL репозиторий для {@link ScheduledTask}.
 */
@Repository
public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {

    List<ScheduledTask> findAllByNextStartTimeIsLessThanEqualAndActiveTrue(LocalDateTime currentDate);

    List<ScheduledTask> findAllByUserAndActiveTrue(User user);

    List<ScheduledTask> findAllByActiveTrue();

}
