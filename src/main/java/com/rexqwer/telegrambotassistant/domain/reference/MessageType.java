package com.rexqwer.telegrambotassistant.domain.reference;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Справочник типов сообщений
 */

@Entity
@Table(name = "message_type")
@Getter
@Setter
public class MessageType implements Serializable {

    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Код
     */
    private Integer code;

    /**
     * Значение
     */
    private String value;

}