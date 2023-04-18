package com.rexqwer.telegrambotassistant.service.message;

import com.rexqwer.telegrambotassistant.domain.Message;
import com.rexqwer.telegrambotassistant.domain.reference.MessageType;
import com.rexqwer.telegrambotassistant.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageTypeService messageTypeService;

    public Message createMessage(MessageType messageType, String chatId, String messageId, String text) {
        Message message = new Message();
        message.setCreatedAt(LocalDateTime.now());
        message.setMessageType(messageType);
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setText(text);
        return saveMessage(message);
    }

    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    public List<Message> findAllLogsOnToday() {
        return messageRepository.findAllByCreatedAtBetweenAndMessageTypeOrderByCreatedAtDesc(
                LocalDate.now().atStartOfDay(), LocalDateTime.now(), messageTypeService.getLogMessage());
    }
}
