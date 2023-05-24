package com.rexqwer.telegrambotassistant.service;

import com.rexqwer.telegrambotassistant.config.ApplicationProperties;
import com.rexqwer.telegrambotassistant.domain.Message;
import com.rexqwer.telegrambotassistant.domain.User;
import com.rexqwer.telegrambotassistant.domain.reference.MessageType;
import com.rexqwer.telegrambotassistant.service.branch.MessageBranchSelector;
import com.rexqwer.telegrambotassistant.service.message.MessageRequestService;
import com.rexqwer.telegrambotassistant.service.message.MessageService;
import com.rexqwer.telegrambotassistant.service.message.MessageTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProcessingService {

    private final UserService userService;
    private final MessageRequestService messageRequestService;
    private final MessageService messageService;
    private final MessageTypeService messageTypeService;
    private final CommandService commandService;
    private final VoiceMessageService voiceMessageService;
    private final MessageBranchSelector messageBranchSelector;
    private final ApplicationProperties applicationProperties;

    public void processMessage(Update update) {
        if (update.getMessage() != null && update.getMessage().getFrom() != null) {
            User user = userService.processUser(update.getMessage().getFrom());
            if (update.getMessage().hasText()) {

                MessageType messageType = messageTypeService.defineMessageType(update.getMessage().getText());
                Message message = messageService.createMessage(
                        user,
                        messageType,
                        update.getMessage().getChatId().toString(),
                        update.getMessage().getMessageId().toString(),
                        update.getMessage().getText()
                );
                messageRequestService.processMessageRequest(message, user);
                if (update.getMessage().isCommand()) {
                    commandService.processCommand(message);
                } else {
                    messageBranchSelector.process(message);
                }
            } else {
                if (update.getMessage().hasVoice() && applicationProperties.getTelegram().getVoice().getActive()) {
                    voiceMessageService.downloadVoiceMessage(update.getMessage().getVoice().getFileId(), user);
                } else {
                    log.error("undefined update message");
                }
            }
        }
    }
}
