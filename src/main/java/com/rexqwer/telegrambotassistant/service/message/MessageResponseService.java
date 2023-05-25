package com.rexqwer.telegrambotassistant.service.message;

import com.rexqwer.telegrambotassistant.domain.MessageResponse;
import com.rexqwer.telegrambotassistant.domain.User;
import com.rexqwer.telegrambotassistant.domain.reference.MessageType;
import com.rexqwer.telegrambotassistant.enums.MessageTypeEnum;
import com.rexqwer.telegrambotassistant.repository.MessageResponseRepository;
import com.rexqwer.telegrambotassistant.repository.MessageTypeRepository;
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
    private final MessageTypeRepository messageTypeRepository;
    private final MessageResponseRepository messageResponseRepository;

    public void processOutgoingMessageResponse(Message tgMessage) {

        MessageType messageType = messageTypeRepository.findByCode(MessageTypeEnum.TELEGRAM_RESPONSE.getCode());
        com.rexqwer.telegrambotassistant.domain.Message message = messageService.createMessage(
                messageService.findOrCreateBotUser(),
                messageType,
                tgMessage.getChatId().toString(),
                tgMessage.getMessageId().toString(),
                tgMessage.getText()
        );

        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessage(message);
        messageResponse.setCreatedAt(message.getCreatedAt());
        messageResponseRepository.save(messageResponse);

        User userByChatId = messageService.findUserByChatId(message.getChatId());

        log.info("Исходящее сообщение для пользователя {}: {}", userByChatId.getUserName(), message.getText());
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
