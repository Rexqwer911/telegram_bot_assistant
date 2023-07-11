package com.rexqwer.telegrambotassistant.domain;

import com.rexqwer.telegrambotassistant.domain.reference.MessageType;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Сообщение
 */

@Entity
@Table(name = "message")
@Getter
@Setter
public class Message implements Serializable {

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
    @JoinColumn(name = "message_type_id")
    private MessageType messageType;

    /**
     * Пользователь
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Ветка сообщений, к которой принадлежит само сообщение
     */
    @ManyToOne
    @JoinColumn(name = "message_branch_id")
    private MessageBranch messageBranch;

    /**
     * Задание по расписанию, которое обращается к данному сообщению
     */
    @ManyToOne
    @JoinColumn(name = "scheduled_task_id")
    private ScheduledTask scheduledTask;

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;

    /**
     *  Текст сообщения
     */
    private String text;

    /**
     * id чата, в котором содержится сообщение
     */
    private String chatId;

    /**
     *  id сообщения в чате
     */
    private String messageId;

}
