package com.rexqwer.telegrambotassistant.repository;

import com.rexqwer.telegrambotassistant.domain.MessageResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data PostgreSQL репозиторий для {@link MessageResponse}.
 */
@Repository
public interface MessageResponseRepository extends JpaRepository<MessageResponse, Long> {

}
