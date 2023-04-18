package com.rexqwer.telegrambotassistant.repository;

import com.rexqwer.telegrambotassistant.domain.Message;
import com.rexqwer.telegrambotassistant.domain.reference.MessageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data PostgreSQL репозиторий для {@link Message}.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findAllByCreatedAtBetweenAndMessageTypeOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to, MessageType messageType);

}