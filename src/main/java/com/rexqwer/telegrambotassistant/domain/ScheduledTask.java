package com.rexqwer.telegrambotassistant.domain;

import com.rexqwer.telegrambotassistant.domain.reference.ScheduledTaskType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Задание, выполняемое по расписанию
 */

@Entity
@Table(name = "scheduled_task")
@Getter
@Setter
public class ScheduledTask implements Serializable {

    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Тип сообщения
     */
    @ManyToOne
    @JoinColumn(name = "scheduled_task_type_id", nullable = false)
    private ScheduledTaskType scheduledTaskType;

    /**
     * Пользователь, для которого зарегистрировано выполнение задания
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Активность задания
     */
    private Boolean active;

    /**
     * Дата и время следующего запуска
     */
    private LocalDateTime nextStartTime;

    /**
     * Паттерн cron
     */
    private String cronPattern;

    /**
     * Сообщение
     */
    @OneToOne
    @JoinColumn(name = "message_id")
    private Message message;
}
