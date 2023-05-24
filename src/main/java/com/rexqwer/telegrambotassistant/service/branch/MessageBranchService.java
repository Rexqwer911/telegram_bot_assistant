package com.rexqwer.telegrambotassistant.service.branch;

import com.rexqwer.telegrambotassistant.domain.Message;
import com.rexqwer.telegrambotassistant.domain.MessageBranch;
import com.rexqwer.telegrambotassistant.domain.reference.MessageBranchType;
import com.rexqwer.telegrambotassistant.event.SendMessageEvent;
import com.rexqwer.telegrambotassistant.repository.MessageBranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageBranchService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final MessageBranchRepository messageBranchRepository;

    public MessageBranch processNewMessageBranch(Message message, MessageBranchType messageBranchType) {
        MessageBranch activeBranch = closeAllBranchesAndCreateNew(message);
        activeBranch.setClosed(false);
        activeBranch.setLocked(false);
        activeBranch.setCreatedAt(message.getCreatedAt());
        activeBranch.setUser(message.getUser());
        activeBranch.setChatId(message.getChatId());
        activeBranch.setMessageBranchType(messageBranchType);
        return messageBranchRepository.save(activeBranch);
    }

    public MessageBranch defineMessageBranch(Message message) {
        MessageBranch activeBranch = findActiveBranch(message);
        if (activeBranch.getId() != null) {
            return activeBranch;
        } else {
            return null;
        }
    }

    public void closeMessageBranch(Message message) {
        applicationEventPublisher.publishEvent(new SendMessageEvent(
                "Ветка сообщений " + message.getMessageBranch().getMessageBranchType().getValue() + " закрыта.",
                message.getChatId(),
                ReplyKeyboardRemove.builder().removeKeyboard(true).build()));
        MessageBranch messageBranch = message.getMessageBranch();
        messageBranch.setClosed(true);
        messageBranchRepository.save(messageBranch);
    }

    private MessageBranch closeAllBranchesAndCreateNew(Message message) {
        List<MessageBranch> activeBranches = messageBranchRepository.findAllByUserAndClosedFalse(message.getUser());
        Mono.just(activeBranches).subscribe(this::closeAndSaveAll);
        return new MessageBranch();
    }
    private MessageBranch findActiveBranch(Message message) {
        List<MessageBranch> activeBranches = messageBranchRepository.findAllByUserAndClosedFalse(message.getUser());
        int activeBranchesCount = activeBranches.size();
        if (activeBranchesCount == 0) {
            return new MessageBranch();
        } else if (activeBranchesCount == 1) {
            return activeBranches.get(0);
        } else {
            applicationEventPublisher.publishEvent(new SendMessageEvent("Обнаружено более одной ветки сообщений. " +
                    "Они будут закрыты, создана новая ветка.", message.getChatId()));
            Mono.just(activeBranches).subscribe(this::closeAndSaveAll);
            return new MessageBranch();
        }
    }

    private void closeAndSaveAll(List<MessageBranch> branches) {
        branches.forEach(messageBranch -> messageBranch.setClosed(true));
        messageBranchRepository.saveAll(branches);
    }
}
