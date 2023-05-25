package com.rexqwer.telegrambotassistant.domain;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Запрос с сообщением
 */

@Entity
@Table(name = "message_request")
@Getter
@Setter
public class MessageRequest implements Serializable {

    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Пользователь
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;

    /**
     * Сообщение
     */
    @OneToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    /**
     * Id чата, из которого пришло сообщение
     */
    private String chatId;


}