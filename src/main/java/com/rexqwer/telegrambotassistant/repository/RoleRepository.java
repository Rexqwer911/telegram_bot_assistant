package com.rexqwer.telegrambotassistant.repository;

import com.rexqwer.telegrambotassistant.domain.reference.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data PostgreSQL репозиторий для {@link Role}.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByCode(Integer code);
}