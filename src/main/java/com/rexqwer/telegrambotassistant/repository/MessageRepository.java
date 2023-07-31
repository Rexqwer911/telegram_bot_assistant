package com.rexqwer.telegrambotassistant.repository;

import com.rexqwer.telegrambotassistant.domain.Message;
import com.rexqwer.telegrambotassistant.domain.reference.MessageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data PostgreSQL репозиторий для {@link Message}.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findAllByCreatedAtBetweenAndMessageTypeOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to, MessageType messageType);

    Optional<Message> findByMessageId(String messageId);

    Optional<Message> findFirstByChatIdOrderByCreatedAtAsc(String chatId);

    @Query(nativeQuery = true, value = "SELECT * FROM message WHERE message.scheduled_task_id = :scheduledTaskId ORDER BY message.created_at DESC OFFSET 1 LIMIT 1")
    Optional<Message> previousSentScheduled(Long scheduledTaskId);
}