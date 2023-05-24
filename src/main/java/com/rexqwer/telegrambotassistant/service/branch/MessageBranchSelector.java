package com.rexqwer.telegrambotassistant.service.branch;

import com.rexqwer.telegrambotassistant.domain.Message;
import com.rexqwer.telegrambotassistant.domain.MessageBranch;
import com.rexqwer.telegrambotassistant.enums.MessageBranchTypeEnum;
import com.rexqwer.telegrambotassistant.event.SendMessageEvent;
import com.rexqwer.telegrambotassistant.repository.MessageBranchRepository;
import com.rexqwer.telegrambotassistant.service.message.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

@Service
@RequiredArgsConstructor
public class MessageBranchSelector {

    private final MessageBranchService messageBranchService;
    private final MessageBranchRepository messageBranchRepository;
    private final MessageService messageService;
    private final ReminderBranchService reminderBranchService;
    private final ChatGPTMessageBranchService chatGPTMessageBranchService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void process(Message message) {
        MessageBranch messageBranch = messageBranchService.defineMessageBranch(message);
        if (messageBranch != null) {
            messageService.assignMessageBranch(message, messageBranch);
            messageBranch = messageBranchRepository.findById(messageBranch.getId()).orElseThrow();
            if (messageBranch.getMessageBranchType().getCode().equals(MessageBranchTypeEnum.REMINDER_BRANCH.getCode())) {
                reminderBranchService.processNewMessage(messageBranch);
            } else if (messageBranch.getMessageBranchType().getCode().equals(MessageBranchTypeEnum.GPT_BRANCH.getCode())) {
                chatGPTMessageBranchService.processNewMessage(messageBranch);
            }
        } else {
            applicationEventPublisher.publishEvent(new SendMessageEvent(
                    "Не понял",
                    message.getChatId(),
                    ReplyKeyboardRemove.builder().removeKeyboard(true).build())
            );
        }
    }
}
