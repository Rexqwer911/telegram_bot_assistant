package com.rexqwer.telegrambotassistant.domain;

import com.rexqwer.telegrambotassistant.domain.reference.Role;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Пользователь
 */

@Entity
@Table(name = "user", schema = "public")
@Getter
@Setter
public class User implements Serializable {

    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    /**
     * id телеграм пользователя
     */
    private String tgId;

    /**
     * Имя пользователя
     */
    private String firstName;

    /**
     * Фамилия пользователя
     */
    private String lastName;

     /**
     * Является ли ботом
     */
    private Boolean isBot;

    /**
     * Логин телеграм
     */
    private String userName;

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;

    /**
     * Дата последнего обновления данных
     */
    private LocalDateTime updatedAt;

//    /**
//     * Хэш пароля
//     */
//    private String pwdHash;

}