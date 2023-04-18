package com.rexqwer.telegrambotassistant.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Отправленное сообщение
 */

@Entity
@Table(name = "message_response")
@Getter
@Setter
public class MessageResponse implements Serializable {

    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

}
