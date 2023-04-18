package com.rexqwer.telegrambotassistant.repository;

import com.rexqwer.telegrambotassistant.domain.reference.MessageBranchType;
import com.rexqwer.telegrambotassistant.domain.reference.MessageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data PostgreSQL репозиторий для {@link MessageBranchType}.
 */
@Repository
public interface MessageBranchTypeRepository extends JpaRepository<MessageBranchType, Long> {

    MessageType findByCode(Integer code);
}
