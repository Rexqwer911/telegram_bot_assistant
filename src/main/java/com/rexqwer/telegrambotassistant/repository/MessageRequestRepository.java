package com.rexqwer.telegrambotassistant.repository;

import com.rexqwer.telegrambotassistant.domain.MessageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data PostgreSQL репозиторий для {@link MessageRequest}.
 */
@Repository
public interface MessageRequestRepository extends JpaRepository<MessageRequest, Long> {

}