package com.rexqwer.telegrambotassistant.service.message;

import com.rexqwer.telegrambotassistant.domain.Message;
import com.rexqwer.telegrambotassistant.domain.MessageRequest;
import com.rexqwer.telegrambotassistant.domain.User;
import com.rexqwer.telegrambotassistant.repository.MessageRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageRequestService {

    private final MessageRequestRepository messageRequestRepository;

    public void processMessageRequest(Message message, User user) {
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setUser(user);
        messageRequest.setCreatedAt(message.getCreatedAt());
        messageRequest.setMessage(message);
        messageRequestRepository.save(messageRequest);
        log.info("Сообщение от {}: {}", user.getUserName(), message.getText());
    }
}
