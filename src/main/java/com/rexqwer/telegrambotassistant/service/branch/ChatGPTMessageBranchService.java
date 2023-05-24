package com.rexqwer.telegrambotassistant.service.branch;

import com.rexqwer.telegrambotassistant.domain.Message;
import com.rexqwer.telegrambotassistant.domain.MessageBranch;
import com.rexqwer.telegrambotassistant.enums.MessageBranchTypeEnum;
import com.rexqwer.telegrambotassistant.enums.MessageTypeEnum;
import com.rexqwer.telegrambotassistant.event.SendMessageEvent;
import com.rexqwer.telegrambotassistant.repository.MessageBranchRepository;
import com.rexqwer.telegrambotassistant.repository.MessageBranchTypeRepository;
import com.rexqwer.telegrambotassistant.repository.MessageRepository;
import com.rexqwer.telegrambotassistant.repository.MessageTypeRepository;
import com.rexqwer.telegrambotassistant.service.ChatGPTService;
import com.rexqwer.telegrambotassistant.service.payload.ContentLengthExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatGPTMessageBranchService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ChatGPTService chatGPTService;
    private final MessageBranchService messageBranchService;
    private final MessageBranchTypeRepository messageBranchTypeRepository;
    private final MessageBranchRepository messageBranchRepository;
    private final MessageTypeRepository messageTypeRepository;
    private final MessageRepository messageRepository;

    public void processNewGptMessageBranch(Message message) {

        ArrayList<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(new KeyboardButton("Прекратить"));
        keyboardRows.add(keyboardRow);

        ReplyKeyboardMarkup replyKeyboardMarkup = ReplyKeyboardMarkup.builder()
                .resizeKeyboard(true)
                .keyboard(keyboardRows)
                .build();

        applicationEventPublisher.publishEvent(new SendMessageEvent(
                "Привет! Я ChatGPT. Можешь со мной пообщаться. Но я работаю достаточно медленно, так что не суди строго.",
                message.getChatId(), replyKeyboardMarkup));

        messageBranchService.processNewMessageBranch(message,
                messageBranchTypeRepository.findByCode(MessageBranchTypeEnum.GPT_BRANCH.getCode()));

    }

    public void processNewMessage(MessageBranch messageBranch) {
        List<Message> messages = messageBranch.getMessages();

        Message message = messages.get(messageBranch.getMessages().size() - 1);

        if (message.getText().equals("Прекратить")) {
            applicationEventPublisher.publishEvent(new SendMessageEvent(
                    "Было приятно пообщаться. До встречи!",
                    message.getChatId(),
                    ReplyKeyboardRemove.builder().removeKeyboard(true).build()));
            messageBranch.setClosed(true);
            messageBranchRepository.save(messageBranch);
        } else {
            if (messageBranch.getLocked()) {
                message.setMessageBranch(null);
                messageRepository.save(message);
                applicationEventPublisher.publishEvent(new SendMessageEvent(
                        "Подождите обработки предыдущего запроса", message.getChatId()));
            } else {
                messageBranch.setLocked(true);
                MessageBranch nMessageBranch = messageBranchRepository.save(messageBranch);
                chatGPTService.executeGPTRequest(nMessageBranch, null)
                        .onErrorResume(ContentLengthExceededException.class, e ->
                                chatGPTService.executeGPTRequest(nMessageBranch, e.getContentLength()))
                        .subscribe(completionResult -> {
                            completionResult.getChoices().forEach(s ->
                                    pushGptResponse(s.getMessage().getContent(), message.getChatId(), nMessageBranch));
                            nMessageBranch.setLocked(false);
                            messageBranchRepository.save(nMessageBranch);
                        });
            }
        }
    }

    private void pushGptResponse(String response, String chatId, MessageBranch messageBranch) {
        Message message = new Message();
        message.setMessageBranch(messageBranch);
        message.setChatId(chatId);
        message.setUser(chatGPTService.findOrCreateGptUser());
        message.setCreatedAt(LocalDateTime.now());
        message.setText(response);
        message.setMessageType(messageTypeRepository.findByCode(MessageTypeEnum.GPT_RESPONSE.getCode()));
        messageRepository.save(message);
        applicationEventPublisher.publishEvent(new SendMessageEvent(response, chatId));
    }
}
