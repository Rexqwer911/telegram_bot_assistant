package com.rexqwer.telegrambotassistant.service.message;

import com.rexqwer.telegrambotassistant.domain.Message;
import com.rexqwer.telegrambotassistant.domain.User;
import com.rexqwer.telegrambotassistant.domain.reference.MessageType;
import com.rexqwer.telegrambotassistant.event.SendMessageEvent;
import com.rexqwer.telegrambotassistant.service.ChatGPTService;
import com.rexqwer.telegrambotassistant.service.CommandService;
import com.rexqwer.telegrambotassistant.service.UserService;
import com.rexqwer.telegrambotassistant.service.branch.MessageBranchSelector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageProcessingService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserService userService;
    private final MessageRequestService messageRequestService;
    private final MessageService messageService;
    private final MessageTypeService messageTypeService;
    private final CommandService commandService;
    private final MessageBranchSelector messageBranchSelector;
    private final ChatGPTService chatGPTService;

    public void processMessage(Update update) {

        if (update.getMessage() != null &&
                update.getMessage().getFrom() != null &&
                update.getMessage().hasText()) {

            User user = userService.processUser(update.getMessage().getFrom());

            MessageType messageType = messageTypeService.defineMessageType(update.getMessage().getText());

            Message message = messageService.createMessage(
                    messageType,
                    update.getMessage().getChatId().toString(),
                    update.getMessage().getMessageId().toString(),
                    update.getMessage().getText()
            );

            messageRequestService.processMessageRequest(message, user);

            String text = update.getMessage().getText();
            if (text.startsWith("/")) {
                commandService.processCommand(message);
            } else {
                if (text.equals("gpt")) {

                    String prompt = "Перескажи сказку Красная шапочка";

                    chatGPTService.executeGPTRequest(prompt).subscribe(completionResult ->
                            completionResult.getChoices().forEach(s ->
                                    applicationEventPublisher.publishEvent(new SendMessageEvent(
                                            s.getText(), update.getMessage().getChatId().toString()))));

                    log.info("Ну типа асинк");

                } else {
                    messageBranchSelector.process(message);
                }
            }
        }
    }
}
