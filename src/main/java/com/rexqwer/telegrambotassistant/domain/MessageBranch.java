package com.rexqwer.telegrambotassistant.domain;

import com.rexqwer.telegrambotassistant.domain.reference.MessageBranchType;
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

    /**
     * Пользователь
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * id чата, в котором содержится ветка
     */
    private String chatId;

    @OneToMany(mappedBy = "messageBranch", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("createdAt ASC")
    private List<Message> messages = new ArrayList<>();

    /**
     * Признак закрытия ветки
     */
    private Boolean closed;

    /**
     * Признак закрытия ветки
     */
    private Boolean locked;

    /**
     * Тип сообщения
     */
    @ManyToOne
    @JoinColumn(name = "message_branch_type_id")
    private MessageBranchType messageBranchType;

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;

    public int getLastMessageIdx() {
        return messages.size() - 1;
    }

    public Message getMessageByIdx(int idx) {
        return messages.get(idx);
    }
}
