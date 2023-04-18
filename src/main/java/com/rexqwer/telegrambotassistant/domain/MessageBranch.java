package com.rexqwer.telegrambotassistant.domain;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "message_branch")
@Getter
@Setter
public class MessageBranch implements Serializable {

    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "messageBranch", cascade = CascadeType.ALL)
    @OrderBy("createdAt ASC")
    private List<Message> messages = new ArrayList<>();

    /**
     * Признак закрытия ветки
     */
    private Boolean closed;

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;
}
