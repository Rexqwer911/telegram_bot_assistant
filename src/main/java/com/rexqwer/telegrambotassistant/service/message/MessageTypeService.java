package com.rexqwer.telegrambotassistant.service.message;

import com.rexqwer.telegrambotassistant.domain.reference.MessageType;
import com.rexqwer.telegrambotassistant.enums.MessageTypeEnum;
import com.rexqwer.telegrambotassistant.repository.MessageTypeRepository;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageTypeService {

    private final MessageTypeRepository messageTypeRepository;
    private MessageType unknownMessage;
    private MessageType logMessage;

    @PostConstruct
    public void init() {
        unknownMessage = messageTypeRepository.findByCode(MessageTypeEnum.UNKNOWN.getCode());
        logMessage = messageTypeRepository.findByCode(MessageTypeEnum.LOG.getCode());
    }

    public MessageType defineMessageType(String text) {
        MessageType messageType;
        if (text.matches(MessageTypeEnum.LOG.getPattern())) {
            messageType = logMessage;
        } else {
            messageType = unknownMessage;
        }
        return messageType;
    }

    public MessageType getLogMessage() {
        return logMessage;
    }
}
