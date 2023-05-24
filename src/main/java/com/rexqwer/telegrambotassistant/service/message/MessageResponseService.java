package com.rexqwer.telegrambotassistant.service.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageResponseService {

    private final MessageService messageService;

    public void processOutgoingMessageResponse(Message tgMessage) {
        //TODO: записывать данные по отправленным сообщениям
        log.info("outgoing");
    }

    public void processOutgoingMessageResponse(Message tgMessage, Long messageId) {
        Optional<com.rexqwer.telegrambotassistant.domain.Message> byId = messageService.findById(messageId);
        if (byId.isPresent()) {
            com.rexqwer.telegrambotassistant.domain.Message message = byId.get();
            message.setMessageId(tgMessage.getMessageId().toString());
            messageService.saveMessage(message);
        } else {
            log.error("Не смогли получить сообщение по id {}", messageId);
        }
    }
}
