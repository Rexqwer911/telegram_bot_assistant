package com.rexqwer.telegrambotassistant.repository;

import com.rexqwer.telegrambotassistant.domain.reference.MessageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data PostgreSQL репозиторий для {@link MessageType}.
 */
@Repository
public interface MessageTypeRepository extends JpaRepository<MessageType, Long> {

    MessageType findByCode(Integer code);

}