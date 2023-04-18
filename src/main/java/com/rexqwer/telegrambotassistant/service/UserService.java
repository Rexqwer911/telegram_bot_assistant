package com.rexqwer.telegrambotassistant.service;

import com.rexqwer.telegrambotassistant.domain.User;
import com.rexqwer.telegrambotassistant.domain.reference.Role;
import com.rexqwer.telegrambotassistant.enums.RoleEnum;
import com.rexqwer.telegrambotassistant.repository.RoleRepository;
import com.rexqwer.telegrambotassistant.repository.UserRepository;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private Role defaultRole;
    private Role adminRole;

    @PostConstruct
    public void init() {
        defaultRole = roleRepository.findByCode(RoleEnum.DEFAULT.getCode());
        adminRole = roleRepository.findByCode(RoleEnum.ADMIN.getCode());
    }

    public User processUser(org.telegram.telegrambots.meta.api.objects.User from) {

        Optional<User> optionalUser = userRepository.findByTgId(from.getId().toString());

        if (optionalUser.isEmpty()) {
            User newUser = new User();
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setTgId(from.getId().toString());
            newUser.setFirstName(from.getFirstName());
            newUser.setLastName(from.getLastName());
            newUser.setIsBot(from.getIsBot());
            newUser.setUserName(from.getUserName());
            newUser.setRoles(Collections.singleton(defaultRole));
            return userRepository.save(newUser);
        } else {
            return optionalUser.get();
        }
    }

    public Role getAdminRole() {
        return adminRole;
    }
}
