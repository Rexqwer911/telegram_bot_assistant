package com.rexqwer.telegrambotassistant.service.message;

import com.rexqwer.telegrambotassistant.domain.Message;
import com.rexqwer.telegrambotassistant.domain.MessageBranch;
import com.rexqwer.telegrambotassistant.domain.ScheduledTask;
import com.rexqwer.telegrambotassistant.domain.User;
import com.rexqwer.telegrambotassistant.domain.reference.MessageType;
import com.rexqwer.telegrambotassistant.enums.RoleEnum;
import com.rexqwer.telegrambotassistant.repository.MessageRepository;
import com.rexqwer.telegrambotassistant.repository.RoleRepository;
import com.rexqwer.telegrambotassistant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageTypeService messageTypeService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public Message createMessage(User user, MessageType messageType, String chatId, String messageId, String text) {
        Message message = new Message();
        message.setUser(user);
        message.setCreatedAt(LocalDateTime.now());
        message.setMessageType(messageType);
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setText(text);
        return saveMessage(message);
    }

    public Message createMessage(User user, MessageType messageType, String text, ScheduledTask scheduledTask) {
        Message message = new Message();
        message.setUser(user);
        message.setCreatedAt(LocalDateTime.now());
        message.setMessageType(messageType);
        message.setChatId(scheduledTask.getMessage().getChatId());
        message.setText(text);
        message.setScheduledTask(scheduledTask);
        return saveMessage(message);
    }

    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    public Message assignMessageBranch(Message message, MessageBranch messageBranch) {
        message.setMessageBranch(messageBranch);
        return messageRepository.save(message);
    }

    public List<Message> findAllLogsOnToday() {
        return messageRepository.findAllByCreatedAtBetweenAndMessageTypeOrderByCreatedAtDesc(
                LocalDate.now().atStartOfDay(), LocalDateTime.now(), messageTypeService.getLogMessage());
    }

    public Optional<Message> findByTgId(String tgId) {
        return messageRepository.findByMessageId(tgId);
    }

    public Optional<Message> findById(Long id) {
        return messageRepository.findById(id);
    }

    public User findOrCreateBotUser() {
        String userGpt = "telegram";
        Optional<User> byTgId = userRepository.findByTgId(userGpt);
        if (byTgId.isPresent()) {
            return byTgId.get();
        } else {
            User newUser = new User();
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setTgId(userGpt);
            newUser.setFirstName(userGpt);
            newUser.setLastName(userGpt);
            newUser.setUserName(userGpt);
            newUser.setRoles(Collections.singleton(roleRepository.findByCode(RoleEnum.SYSTEM.getCode())));
            return userRepository.save(newUser);
        }
    }

    public User findUserByChatId(String chatId) {
        return messageRepository.findFirstByChatIdOrderByCreatedAtAsc(chatId).map(Message::getUser).orElse(null);
    }

    public Message findPreviousMessageForScheduledTask(ScheduledTask scheduledTask) {
        return messageRepository.previousSentScheduled(scheduledTask.getId()).orElse(null);
    }
}
