package com.rexqwer.telegrambotassistant.repository;

import com.rexqwer.telegrambotassistant.domain.MessageBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data PostgreSQL репозиторий для {@link MessageBranch}.
 */
@Repository
public interface MessageBranchRepository extends JpaRepository<MessageBranch, Long> {

    List<MessageBranch> findAllByClosedFalse();

}
