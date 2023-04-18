package com.rexqwer.telegrambotassistant.domain.reference;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Роли пользователей
 */

@Entity
@Table(name = "role")
@Getter
@Setter
public class Role implements Serializable {

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
     * Имя
     */
    private String name;

}