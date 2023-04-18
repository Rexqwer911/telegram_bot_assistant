package com.rexqwer.telegrambotassistant.repository;

import com.rexqwer.telegrambotassistant.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data PostgreSQL репозиторий для {@link User}.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByTgId(String tgId);
}